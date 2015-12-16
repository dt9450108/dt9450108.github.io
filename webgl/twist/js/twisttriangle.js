var gl;
var points = [];
var canvas = null;
var TWIST_ENABLE = 1;
var THETA = 0 * Math.PI / 180;
var DIVIDE_NUM = 1;
var TRIANGLE_SCALE = 1;
var colors = [];
var colord = vec4(0.0, 0.37254902, 0.392156863, 1.0);
var mode = 1;

$(document).ready(function() {
    $('#twistyes').click(function() {
        TWIST_ENABLE = !TWIST_ENABLE;
        $('#twistyes').children('span').removeClass("glyphicon glyphicon-remove").addClass("glyphicon glyphicon-ok");
        $('#twistno').children('span').removeClass("glyphicon glyphicon-ok").addClass("glyphicon glyphicon-remove");
        redraw();
    });

    $('#twistno').click(function() {
        TWIST_ENABLE = !TWIST_ENABLE;
        $('#twistyes').children('span').removeClass("glyphicon glyphicon-ok").addClass("glyphicon glyphicon-remove");
        $('#twistno').children('span').removeClass("glyphicon glyphicon-remove").addClass("glyphicon glyphicon-ok");
        redraw();
    });

    $("#scaleSlider").slider({
        'slide': function(event, ui) {
            $('#scaleInput').val((ui.value * 0.01).toFixed(2));
            redraw();
        },
        'value': 100,
        'max': 200,
        'min': 10
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

    $("#degreeSlider").slider({
        'slide': function(event, ui) {
            $('#degreeInput').val(ui.value);
            redraw();
        },
        'value': 0,
        'max': 180,
        'min': -180
    });
    $('#degreeInput').change(function(event) {
        if (this.value < -180)
            $('#degreeInput').val(-180);
        else if (this.value > 180)
            $('#degreeInput').val(180);
        redraw();
    });

    $('#pcolorLabel').colorpicker({
        'format': 'rgba',
        'customClass': 'colorpicker-2x',
        'color': 'rgba(0,96,100, 1.0)',
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
    $('#pcolorLabel').on('changeColor.colorpicker', setLabelColor);
    $('#pcolorLabel').css('background-color', 'rgba(0,96,100, 1.0)');
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
    // var vertices = new Float32Array([-1, -1, 0, 1, 1, -1]);
    // var vertices = [vec2(-1, -1), vec2(0, 1), vec2(1, -1)];
    var point1 = vec2(0, Math.sqrt(3) / 3 * TRIANGLE_SCALE);
    var point2 = vec2(-0.5 * TRIANGLE_SCALE, -0.5 / Math.sqrt(3) * TRIANGLE_SCALE);
    var point3 = vec2(0.5 * TRIANGLE_SCALE, -0.5 / Math.sqrt(3) * TRIANGLE_SCALE);

    divideTriangle(point1, point2, point3, DIVIDE_NUM);

    //  Configure WebGL
    gl.viewport(0, 0, canvas.width, canvas.height);
    gl.clearColor(1.0, 1.0, 1.0, 1.0);

    //  Load shaders and initialize attribute buffers
    var program = initShaders(gl, "vertex-shader", "fragment-shader");
    gl.useProgram(program);

    var cBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, cBuffer);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(colors), gl.STATIC_DRAW);
    var vColor = gl.getAttribLocation(program, "vColor");
    gl.vertexAttribPointer(vColor, 4, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vColor);

    // Load the data into the GPU
    var bufferId = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, bufferId);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(points), gl.STATIC_DRAW);
    var vPosition = gl.getAttribLocation(program, "vPosition");
    gl.vertexAttribPointer(vPosition, 2, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vPosition);

    render();
}

function redraw() {
    TRIANGLE_SCALE = $('#scaleInput').val();
    DIVIDE_NUM = $('#depthInput').val();
    THETA = $('#degreeInput').val() * Math.PI / 180;

    draw();
}

function render() {
    gl.clear(gl.COLOR_BUFFER_BIT);
    if (mode)
        gl.drawArrays(gl.TRIANGLES, 0, points.length);
    else
        for (var i = 0; i < points.length; i += 3)
            gl.drawArrays(gl.LINE_LOOP, i, 3);
}

function divideTriangle(a, b, c, num) {
    var avg = function(x, y) {
        return vec2(((x[0] + y[0]) * 0.5), ((x[1] + y[1]) * 0.5));
    };
    if (num > 0) {
        var x = avg(a, b);
        var y = avg(b, c);
        var z = avg(a, c);
        divideTriangle(a, x, z, num - 1);
        divideTriangle(x, b, y, num - 1);
        divideTriangle(z, y, c, num - 1);
        divideTriangle(x, y, z, num - 1);
    } else {
        triangle(a, b, c);
    }
}

function triangle(a, b, c) {
    colors.push(colord, colord, colord);
    points.push(twist(a), twist(b), twist(c));
}

function twist(p) {
    /*
    x'= x cos(dθ) − y sin(dθ)
    y'= x sin(dθ) + y cos(dθ)
     */
    var d = TWIST_ENABLE ? Math.sqrt(p[0] * p[0] + p[1] * p[1]) : 1;
    var dtheta = d * THETA;
    var sin = Math.sin(dtheta);
    var cos = Math.cos(dtheta);
    return vec2((p[0] * cos - p[1] * sin), (p[0] * sin + p[1] * cos));
}

function setLabelColor(event) {
    var rgb = event.color.toRGB();
    $('#pcolorLabel').css('background-color', 'rgba(' + rgb.r + ', ' + rgb.g + ', ' + rgb.b + ', ' + rgb.a + ')');

    if (rgb.a <= 0.4 || (rgb.r == 255 || rgb.g == 255 || rgb.b == 255))
        $('#pcolorLabel').css('color', '#fff');
    else if (rgb.a <= 0.4 || (rgb.r >= 200 || rgb.g >= 200 || rgb.b >= 200))
        $('#pcolorLabel').css('color', '#000');
    else
        $('#pcolorLabel').css('color', '#fff');
    colord = vec4(rgb.r / 255, rgb.g / 255, rgb.b / 255, rgb.a);
    redraw();
}

