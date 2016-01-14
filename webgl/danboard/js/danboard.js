var canvas;
var gl;
var program;
var projectionMatrix;
var modelViewMatrix;
var instanceMatrix;
var modelViewMatrixLoc;
var vertices = [
    vec4(-0.5, -0.5, 0.5, 1.0),
    vec4(-0.5, 0.5, 0.5, 1.0),
    vec4(0.5, 0.5, 0.5, 1.0),
    vec4(0.5, -0.5, 0.5, 1.0),
    vec4(-0.5, -0.5, -0.5, 1.0),
    vec4(-0.5, 0.5, -0.5, 1.0),
    vec4(0.5, 0.5, -0.5, 1.0),
    vec4(0.5, -0.5, -0.5, 1.0)
];
var vertexColors = [
    vec4(0.129411765, 0.588235294, 0.952941176, 1.0),
    vec4(0.129411765, 0.588235294, 0.952941176, 1.0),
    vec4(0.129411765, 0.588235294, 0.952941176, 1.0),
    vec4(0.129411765, 0.588235294, 0.952941176, 1.0),
    vec4(0.050980392, 0.278431373, 0.631372549, 1.0),
    vec4(0.050980392, 0.278431373, 0.631372549, 1.0),
    vec4(0.050980392, 0.278431373, 0.631372549, 1.0),
    vec4(0.050980392, 0.278431373, 0.631372549, 1.0)
];

var torsoId = 0;
var headId = 1;
var head2Id = 10;
var leftUpperArmId = 2;
var leftLowerArmId = 3;
var rightUpperArmId = 4;
var rightLowerArmId = 5;
var leftUpperLegId = 6;
var leftLowerLegId = 7;
var rightUpperLegId = 8;
var rightLowerLegId = 9;

var torsoHeight = 4.0;
var torsoWidth = 3.0;
var upperArmHeight = -2.0;
var lowerArmHeight = -2.0;
var upperArmWidth = 1.0;
var lowerArmWidth = 1.0;
var upperLegWidth = 1.0;
var lowerLegWidth = 1.0;
var lowerLegHeight = 2.0;
var upperLegHeight = 2.0;
var headHeight = 5.0;
var headWidth = 5.0;

var numNodes = 10;
var numAngles = 11;
var angle = 0;
var theta = [180, 0, 0, 0, 0, 0, 0, 0, 0, 0, 180];
var stack = [];
var figure = [];

var vBuffer;
var modelViewLoc;
var pointsArray = [];
var colors = [];

var walkAnimInterval = null;
var walkLatch = 0;
var walkTime = 0;
var runAnimInterval = null;
var runLatch = 0;
var runTime = 0;

$(document).ready(function() {
    for (var i = 0; i < numNodes; i++)
        figure[i] = createNode(null, null, null, null);

    init();

    // event
    $("#bodyRotationSlider").slider({
        'slide': function(event, ui) {
            theta[torsoId] = -ui.value + 180;
            initNodes(torsoId);
        },
        'value': 0,
        'max': 360,
        'min': -360,
        'step': 1
    });

    $("#headRotationSlider").slider({
        'slide': function(event, ui) {
            theta[head2Id] = -ui.value + 180;
            initNodes(head2Id);
        },
        'value': 0,
        'max': 180,
        'min': -180,
        'step': 1
    });

    $("#walkSlider").slider({
        'slide': function(event, ui) {
            theta[leftUpperArmId] = ui.value;
            initNodes(leftUpperArmId);
        },
        'value': 0,
        'max': 10,
        'min': -10,
        'step': 1
    });

    $("#runSlider").slider({
        'slide': function(event, ui) {
            theta[leftUpperArmId] = ui.value;
            initNodes(leftUpperArmId);
        },
        'value': 0,
        'max': 60,
        'min': -60,
        'step': 1
    });

    // animation btn
    $("#walkAnimationBtn").click(function() {
        $("#walkSlider").slider({
            disabled: true
        });
        $("#runSlider").slider({
            disabled: true
        });

        if (runAnimInterval != null) {
            clearInterval(runAnimInterval);
            runAnimInterval = null;
        }
        walkAnimInterval = setInterval(function() {
            if (walkLatch) {
                // positive
                theta[leftUpperArmId] = walkTime++;
                initNodes(leftUpperArmId);

                if (walkTime == 10)
                    walkLatch = !walkLatch;
            } else {
                // negative
                theta[leftUpperArmId] = walkTime--;
                initNodes(leftUpperArmId);

                if (walkTime == -10)
                    walkLatch = !walkLatch;
            }
        }, 50);
    });

    $("#runAnimationBtn").click(function() {
        $("#walkSlider").slider({
            disabled: true
        });
        $("#runSlider").slider({
            disabled: true
        });
        if (walkAnimInterval != null) {
            clearInterval(walkAnimInterval);
            walkAnimInterval = null;
        }
        runAnimInterval = setInterval(function() {
            if (runLatch) {
                // positive
                theta[leftUpperArmId] = runTime++;
                initNodes(leftUpperArmId);

                if (runTime == 60)
                    runLatch = !runLatch;
            } else {
                // negative
                theta[leftUpperArmId] = runTime--;
                initNodes(leftUpperArmId);

                if (runTime == -60)
                    runLatch = !runLatch;
            }
        }, 1);
    });

    $("#cancelAnimationBtn").click(function() {
        $("#walkSlider").slider({
            disabled: false
        });
        $("#runSlider").slider({
            disabled: false
        });
        if (walkAnimInterval != null) {
            clearInterval(walkAnimInterval);
            walkAnimInterval = null;
            theta[leftUpperArmId] = 0;
            initNodes(leftUpperArmId);
        }
        if (runAnimInterval != null) {
            clearInterval(runAnimInterval);
            runAnimInterval = null;
            theta[leftUpperArmId] = 0;
            initNodes(leftUpperArmId);
        }
    });
});

function init() {
    canvas = document.getElementById("gl-canvas");
    gl = WebGLUtils.setupWebGL(canvas);
    if (!gl) {
        alert("WebGL isn't available");
    }
    gl.viewport(0, 0, canvas.width, canvas.height);
    gl.clearColor(1.0, 1.0, 1.0, 1.0);
    gl.enable(gl.DEPTH_TEST);
    //
    //  Load shaders and initialize attribute buffers
    //
    program = initShaders(gl, "vertex-shader", "fragment-shader");
    gl.useProgram(program);
    instanceMatrix = mat4();
    projectionMatrix = ortho(-10.0, 10.0, -10.0, 10.0, -10.0, 10.0);
    modelViewMatrix = mat4();

    gl.uniformMatrix4fv(gl.getUniformLocation(program, "modelViewMatrix"), false, flatten(modelViewMatrix));
    gl.uniformMatrix4fv(gl.getUniformLocation(program, "projectionMatrix"), false, flatten(projectionMatrix));
    modelViewMatrixLoc = gl.getUniformLocation(program, "modelViewMatrix");

    cube();

    // vertice
    vBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vBuffer);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(pointsArray), gl.STATIC_DRAW);
    var vPosition = gl.getAttribLocation(program, "vPosition");
    gl.vertexAttribPointer(vPosition, 4, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vPosition);

    // color
    cBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, cBuffer);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(colors), gl.STATIC_DRAW);
    var vColor = gl.getAttribLocation(program, "vColor");
    gl.vertexAttribPointer(vColor, 4, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vColor);

    for (i = 0; i < numNodes; i++)
        initNodes(i);
    render();
}

//-------------------------------------------

function scale4(a, b, c) {
    var result = mat4();
    result[0][0] = a;
    result[1][1] = b;
    result[2][2] = c;
    return result;
}

//--------------------------------------------

function createNode(transform, render, sibling, child) {
    var node = {
        transform: transform,
        render: render,
        sibling: sibling,
        child: child
    }
    return node;
}

function initNodes(Id) {
    var m = mat4();
    switch (Id) {
        case torsoId:
            m = rotate(theta[torsoId], 0, 1, 0);
            figure[torsoId] = createNode(m, torso, null, headId);
            break;
        case headId:
        case head2Id:
            m = translate(0.0, torsoHeight + 0.5 * headHeight, 0.0);
            m = mult(m, rotate(theta[head2Id], 0, 1, 0));
            m = mult(m, translate(0.0, -0.5 * headHeight, 0.0));
            figure[headId] = createNode(m, head, leftUpperArmId, null);
            break;
        case leftUpperArmId:
            // left upper arm
            m = translate(-(torsoWidth * 0.39 + upperArmWidth), 0.9 * torsoHeight, 0.0);
            m = mult(m, rotate(theta[leftUpperArmId], 1, 0, 0));
            figure[leftUpperArmId] = createNode(m, leftUpperArm, rightUpperArmId, leftLowerArmId);

            m = translate(0.0, upperArmHeight, 0.0);
            if (theta[leftUpperArmId] >= 0) {
                m = mult(m, rotate(theta[leftUpperArmId], 1, 0, 0));
            } else {
                m = mult(m, rotate(-theta[leftUpperArmId], 1, 0, 0));
            }
            figure[leftLowerArmId] = createNode(m, leftLowerArm, null, null);

            // right upper arm
            theta[rightUpperArmId] = -theta[leftUpperArmId];
            m = translate(torsoWidth * 0.39 + upperArmWidth, 0.9 * torsoHeight, 0.0);
            m = mult(m, rotate(theta[rightUpperArmId], 1, 0, 0));
            figure[rightUpperArmId] = createNode(m, rightUpperArm, leftUpperLegId, rightLowerArmId);
            m = translate(0.0, upperArmHeight, 0.0);
            if (theta[rightUpperArmId] >= 0) {
                m = mult(m, rotate(theta[rightLowerArmId] + 1 * theta[rightUpperArmId], 1, 0, 0));
            } else {
                m = mult(m, rotate(theta[rightLowerArmId] - 1 * theta[rightUpperArmId], 1, 0, 0));
            }
            figure[rightLowerArmId] = createNode(m, rightLowerArm, null, null);

            // left upper leg
            theta[leftUpperLegId] = -theta[rightUpperArmId];
            m = translate(-(torsoWidth * -0.05 + upperLegWidth), -0.1 * upperLegHeight, 0.0);
            m = mult(m, rotate(180 - theta[leftUpperLegId], 1, 0, 0));
            figure[leftUpperLegId] = createNode(m, leftUpperLeg, rightUpperLegId, leftLowerLegId);

            m = translate(0.0, upperLegHeight, 0.0);
            if (theta[leftUpperLegId] >= 0) {
                m = mult(m, rotate(-theta[leftUpperLegId], 1, 0, 0));
            } else {
                m = mult(m, rotate(theta[leftUpperLegId], 1, 0, 0));
            }
            figure[leftLowerLegId] = createNode(m, leftLowerLeg, null, null);

            // right upper leg
            theta[rightUpperLegId] = -theta[leftUpperArmId];
            m = translate(torsoWidth * -0.05 + upperLegWidth, -0.1 * upperLegHeight, 0.0);
            m = mult(m, rotate(180 - theta[rightUpperLegId], 1, 0, 0));
            figure[rightUpperLegId] = createNode(m, rightUpperLeg, null, rightLowerLegId);

            m = translate(0.0, upperLegHeight, 0.0);
            if (theta[rightUpperLegId] >= 0) {
                m = mult(m, rotate(-theta[rightUpperLegId], 1, 0, 0));
            } else {
                m = mult(m, rotate(theta[rightUpperLegId], 1, 0, 0));
            }
            figure[rightLowerLegId] = createNode(m, rightLowerLeg, null, null);
            break;
    }
}

function traverse(Id) {
    if (Id == null)
        return;
    stack.push(modelViewMatrix);
    modelViewMatrix = mult(modelViewMatrix, figure[Id].transform);
    figure[Id].render();
    if (figure[Id].child != null)
        traverse(figure[Id].child);
    modelViewMatrix = stack.pop();
    if (figure[Id].sibling != null)
        traverse(figure[Id].sibling);
}

function torso() {
    instanceMatrix = mult(modelViewMatrix, translate(0.0, 0.5 * torsoHeight, 0.0));
    instanceMatrix = mult(instanceMatrix, scale4(torsoWidth, torsoHeight, torsoWidth));
    gl.uniformMatrix4fv(modelViewMatrixLoc, false, flatten(instanceMatrix));

    for (var i = 0; i < 6; i++)
        gl.drawArrays(gl.TRIANGLE_FAN, 4 * i, 4);
}

function head() {
    instanceMatrix = mult(modelViewMatrix, translate(0.0, 0.5 * headHeight, 0.0));
    instanceMatrix = mult(instanceMatrix, scale4(headWidth, headHeight, headWidth));
    gl.uniformMatrix4fv(modelViewMatrixLoc, false, flatten(instanceMatrix));

    for (var i = 0; i < 6; i++)
        gl.drawArrays(gl.TRIANGLE_FAN, 4 * i, 4);
}

function leftUpperArm() {
    instanceMatrix = mult(modelViewMatrix, translate(0.0, 0.5 * upperArmHeight, 0.0));
    instanceMatrix = mult(instanceMatrix, scale4(upperArmWidth, upperArmHeight, upperArmWidth));
    gl.uniformMatrix4fv(modelViewMatrixLoc, false, flatten(instanceMatrix));

    for (var i = 0; i < 6; i++)
        gl.drawArrays(gl.TRIANGLE_FAN, 4 * i, 4);
}

function leftLowerArm() {
    instanceMatrix = mult(modelViewMatrix, translate(0.0, 0.5 * lowerArmHeight, 0.0));
    instanceMatrix = mult(instanceMatrix, scale4(lowerArmWidth, lowerArmHeight, lowerArmWidth));
    gl.uniformMatrix4fv(modelViewMatrixLoc, false, flatten(instanceMatrix));

    for (var i = 0; i < 6; i++)
        gl.drawArrays(gl.TRIANGLE_FAN, 4 * i, 4);
}

function rightUpperArm() {
    instanceMatrix = mult(modelViewMatrix, translate(0.0, 0.5 * upperArmHeight, 0.0));
    instanceMatrix = mult(instanceMatrix, scale4(upperArmWidth, upperArmHeight, upperArmWidth));
    gl.uniformMatrix4fv(modelViewMatrixLoc, false, flatten(instanceMatrix));

    for (var i = 0; i < 6; i++)
        gl.drawArrays(gl.TRIANGLE_FAN, 4 * i, 4);
}

function rightLowerArm() {
    instanceMatrix = mult(modelViewMatrix, translate(0.0, 0.5 * lowerArmHeight, 0.0));
    instanceMatrix = mult(instanceMatrix, scale4(lowerArmWidth, lowerArmHeight, lowerArmWidth));
    gl.uniformMatrix4fv(modelViewMatrixLoc, false, flatten(instanceMatrix));

    for (var i = 0; i < 6; i++)
        gl.drawArrays(gl.TRIANGLE_FAN, 4 * i, 4);
}

function leftUpperLeg() {
    instanceMatrix = mult(modelViewMatrix, translate(0.0, 0.5 * upperLegHeight, 0.0));
    instanceMatrix = mult(instanceMatrix, scale4(upperLegWidth, upperLegHeight, upperLegWidth));
    gl.uniformMatrix4fv(modelViewMatrixLoc, false, flatten(instanceMatrix));

    for (var i = 0; i < 6; i++)
        gl.drawArrays(gl.TRIANGLE_FAN, 4 * i, 4);
}

function leftLowerLeg() {
    instanceMatrix = mult(modelViewMatrix, translate(0.0, 0.5 * lowerLegHeight, 0.0));
    instanceMatrix = mult(instanceMatrix, scale4(lowerLegWidth, lowerLegHeight, lowerLegWidth));
    gl.uniformMatrix4fv(modelViewMatrixLoc, false, flatten(instanceMatrix));

    for (var i = 0; i < 6; i++)
        gl.drawArrays(gl.TRIANGLE_FAN, 4 * i, 4);
}

function rightUpperLeg() {
    instanceMatrix = mult(modelViewMatrix, translate(0.0, 0.5 * upperLegHeight, 0.0));
    instanceMatrix = mult(instanceMatrix, scale4(upperLegWidth, upperLegHeight, upperLegWidth));
    gl.uniformMatrix4fv(modelViewMatrixLoc, false, flatten(instanceMatrix));

    for (var i = 0; i < 6; i++)
        gl.drawArrays(gl.TRIANGLE_FAN, 4 * i, 4);
}

function rightLowerLeg() {
    instanceMatrix = mult(modelViewMatrix, translate(0.0, 0.5 * lowerLegHeight, 0.0));
    instanceMatrix = mult(instanceMatrix, scale4(lowerLegWidth, lowerLegHeight, lowerLegWidth))
    gl.uniformMatrix4fv(modelViewMatrixLoc, false, flatten(instanceMatrix));

    for (var i = 0; i < 6; i++)
        gl.drawArrays(gl.TRIANGLE_FAN, 4 * i, 4);
}

function quad(a, b, c, d) {
    colors.push(vertexColors[a]);
    pointsArray.push(vertices[a]);

    colors.push(vertexColors[b]);
    pointsArray.push(vertices[b]);

    colors.push(vertexColors[c]);
    pointsArray.push(vertices[c]);

    colors.push(vertexColors[d]);
    pointsArray.push(vertices[d]);
}

function cube() {
    quad(1, 0, 3, 2);
    quad(2, 3, 7, 6);
    quad(3, 0, 4, 7);
    quad(6, 5, 1, 2);
    quad(4, 5, 6, 7);
    quad(5, 4, 0, 1);
}

var render = function() {
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    traverse(torsoId);
    requestAnimFrame(render);
}
