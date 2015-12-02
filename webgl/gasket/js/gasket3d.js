var gl;
var canvas = null;
var points = [];
var colors = [];
var DIVIDE_NUM = 1;
var PYRAMID_SCALE = 1;
var surfaceColors = [vec3(0.50196, 0.79608, 0.76863),
    vec3(0.0, 0.30196, 0.25098),
    vec3(0.0, 0.53725, 0.48235),
    vec3(0.0, 0.0, 0.0)
];
var mode = 1;

$(document).ready(function() {
    $("#scaleSlider").slider({
        'slide': function(event, ui) {
            $('#scaleInput').val(ui.value);
            redraw();
        },
        'value': 1,
        'max': 1.64,
        'min': 1,
        'step': 0.01
    });
    $('#scaleInput').change(function(event) {
        if (this.value < 1)
            $('#scaleInput').val(1);
        else if (this.value > 3)
            $('#scaleInput').val(3);
        redraw();
    });

    $("#depthSlider").slider({
        'slide': function(event, ui) {
            $('#depthInput').val(ui.value);
            redraw();
        },
        'value': 1,
        'max': 8,
        'min': 1
    });
    $('#depthInput').change(function(event) {
        if (this.value < 1)
            $('#depthInput').val(1);
        else if (this.value > 8)
            $('#depthInput').val(8);
        redraw();
    });

    $('#surfaceColor1').colorpicker({
        'format': 'rgba',
        'customClass': 'colorpicker-2x',
        'color': 'rgba(128,203,196, 1.0)',
        'sliders': {
            'saturation': {
                'maxLeft': 125,
                'maxTop': 125
            },
            'hue': {
                'maxTop': 125
            },
            'alpha': {
                'maxTop': 125
            }
        }
    });
    $('#surfaceColor1').on('changeColor.colorpicker', function(event) {
        setLabelColor(event, 1);
    });
    $('#surfaceColor1').css('background-color', 'rgba(128,203,196, 1.0)');

    $('#surfaceColor2').colorpicker({
        'format': 'rgba',
        'customClass': 'colorpicker-2x',
        'color': 'rgba(0,137,123, 1.0)',
        'sliders': {
            'saturation': {
                'maxLeft': 125,
                'maxTop': 125
            },
            'hue': {
                'maxTop': 125
            },
            'alpha': {
                'maxTop': 125
            }
        }
    });
    $('#surfaceColor2').on('changeColor.colorpicker', function(event) {
        setLabelColor(event, 2);
    });
    $('#surfaceColor2').css('background-color', 'rgba(0,137,123, 1.0)');

    $('#surfaceColor3').colorpicker({
        'format': 'rgba',
        'customClass': 'colorpicker-2x',
        'color': 'rgba(0,77,64, 1.0)',
        'sliders': {
            'saturation': {
                'maxLeft': 125,
                'maxTop': 125
            },
            'hue': {
                'maxTop': 125
            },
            'alpha': {
                'maxTop': 125
            }
        }
    });
    $('#surfaceColor3').on('changeColor.colorpicker', function(event) {
        setLabelColor(event, 3);
    });
    $('#surfaceColor3').css('background-color', 'rgba(0,77,64, 1.0)');

    $('#changemode').click(function() {
        mode = !mode;
        redraw();
    })
});

window.onload = function init() {
    canvas = document.getElementById("twistcanvas");

    gl = WebGLUtils.setupWebGL(canvas);
    if (!gl) {
        console.log("WebGL isn't available");
    }
    draw();
};

function draw() {
    points = [];
    colors = [];

    /*
    (1/2, -√3/6, -√6/12)
    (-1/2, -√3/6, -√6/12)
    (0, √3/3, -√6/12)
    (0, 0, √6/4)
     */
    var sqrtSix = Math.sqrt(6);
    var sqrtThree = Math.sqrt(3);
    var vertices = [
        vec3(0.0, 0.0, -sqrtSix / 4.0 * PYRAMID_SCALE),
        vec3(0.0, sqrtThree / 3.0 * PYRAMID_SCALE, sqrtSix / 12.0 * PYRAMID_SCALE),
        vec3(-0.5 * PYRAMID_SCALE, -sqrtThree / 6.0 * PYRAMID_SCALE, sqrtSix / 12.0 * PYRAMID_SCALE),
        vec3(0.5 * PYRAMID_SCALE, -sqrtThree / 6.0 * PYRAMID_SCALE, sqrtSix / 12.0 * PYRAMID_SCALE)
    ];

    dividePyramid(vertices[0], vertices[1], vertices[2], vertices[3], DIVIDE_NUM);

    //  Configure WebGL
    gl.viewport(0, 0, canvas.width, canvas.height);
    gl.clearColor(1.0, 1.0, 1.0, 1.0);

    gl.enable(gl.DEPTH_TEST);

    //  Load shaders and initialize attribute buffers
    var program = initShaders(gl, "vertex-shader", "fragment-shader");
    gl.useProgram(program);

    var cBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, cBuffer);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(colors), gl.STATIC_DRAW);
    var vColor = gl.getAttribLocation(program, "vColor");
    gl.vertexAttribPointer(vColor, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vColor);

    // Load the data into the GPU
    var bufferId = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, bufferId);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(points), gl.STATIC_DRAW);
    var vPosition = gl.getAttribLocation(program, "vPosition");
    gl.vertexAttribPointer(vPosition, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vPosition);

    render();
}

function redraw() {
    PYRAMID_SCALE = $('#scaleInput').val();
    DIVIDE_NUM = $('#depthInput').val();

    draw();
}

function render() {
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    if (mode)
        gl.drawArrays(gl.TRIANGLES, 0, points.length);
    else
        for (var i = 0; i < points.length; i += 3)
            gl.drawArrays(gl.LINE_LOOP, i, 3);
}

function dividePyramid(a, b, c, d, num) {
    if (num == 0) {
        pushPyramid(a, b, c, d);
    } else {
        var ab = mix(a, b, 0.5);
        var ac = mix(a, c, 0.5);
        var ad = mix(a, d, 0.5);
        var bc = mix(b, c, 0.5);
        var bd = mix(b, d, 0.5);
        var cd = mix(c, d, 0.5);

        --num;

        dividePyramid(a, ab, ac, ad, num);
        dividePyramid(ab, b, bc, bd, num);
        dividePyramid(ac, bc, c, cd, num);
        dividePyramid(ad, bd, cd, d, num);
    }
}

function pushPyramid(a, b, c, d) {
    // front left surface
    pyramid(a, c, b, 0);

    // front right surface
    pyramid(a, c, d, 1);

    // front bottom surface
    pyramid(a, b, d, 2);

    // back surface
    pyramid(b, c, d, 3);
}

function pyramid(a, b, c, color) {
    colors.push(surfaceColors[color]);
    points.push(a);

    colors.push(surfaceColors[color]);
    points.push(b);

    colors.push(surfaceColors[color]);
    points.push(c);
}

function setLabelColor(event, surface) {
    var rgb = event.color.toRGB();
    $('#surfaceColor' + surface).css('background-color', 'rgba(' + rgb.r + ', ' + rgb.g + ', ' + rgb.b + ', ' + rgb.a + ')');

    if (rgb.a <= 0.4 || (rgb.r == 255 || rgb.g == 255 || rgb.b == 255))
        $('#surfaceColor' + surface).css('color', '#fff');
    else if (rgb.a <= 0.4 || (rgb.r >= 200 || rgb.g >= 200 || rgb.b >= 200))
        $('#surfaceColor' + surface).css('color', '#000');
    else
        $('#surfaceColor' + surface).css('color', '#fff');
    surfaceColors[surface - 1] = vec3(rgb.r / 255, rgb.g / 255, rgb.b / 255);
    redraw();
}
