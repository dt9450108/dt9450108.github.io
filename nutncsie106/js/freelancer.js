// Freelancer Theme JavaScript

(function($) {
    "use strict"; // Start of use strict

    // jQuery for page scrolling feature - requires jQuery Easing plugin
    $('.page-scroll a').bind('click', function(event) {
        var $anchor = $(this);
        $('html, body').stop().animate({
            scrollTop: ($($anchor.attr('href')).offset().top - 50)
        }, 1250, 'easeInOutExpo');
        event.preventDefault();
    });

    // Highlight the top nav as scrolling occurs
    $('body').scrollspy({
        target: '.navbar-fixed-top',
        offset: 51
    });

    // Closes the Responsive Menu on Menu Item Click
    $('.navbar-collapse ul li a').click(function() {
        $('.navbar-toggle:visible').click();
    });

    // Offset for Main Navigation
    $('#mainNav').affix({
        offset: {
            top: 100
        }
    })

    // Floating label headings for the contact form
    $(function() {
        $("body").on("input propertychange", ".floating-label-form-group", function(e) {
            $(this).toggleClass("floating-label-form-group-with-value", !!$(e.target).val());
        }).on("focus", ".floating-label-form-group", function() {
            $(this).addClass("floating-label-form-group-with-focus");
        }).on("blur", ".floating-label-form-group", function() {
            $(this).removeClass("floating-label-form-group-with-focus");
        });
    });

    // Add portfolio contents
    initPortfolio();
})(jQuery); // End of use strict

function initPortfolio() {
    for (var i = 0; i < Projects.length; i++) {
        $('#portfolio-content').append(
            '<div class="col-sm-4 portfolio-item">' +
            '<a class="portfolio-link" onclick="ShowProjectDialog(' + i + ');"><div class="caption">' +
            '<div class="caption-content"><i class="fa fa-search-plus fa-3x"></i></div></div>' +
            '<img src="img/portfolio/' + (Projects[i].img == null ? "no_photo.jpg" : Projects[i].id + '/' + Projects[i].img[0] + '.jpg') + '" class="img-responsive" alt="' + Projects[i].title_zhtw + '"></a>' +
            '<div class="portfolio-item-info"><div><p class="portfolio-item-info-header height-large">' + Projects[i].title_zhtw +
            '</p></div><div><p class="portfolio-item-info-header">學生：</p>' +
            '<p class="portfolio-item-info-content">' + Projects[i].student + '</p></div><div><p class="portfolio-item-info-header">指導教授：</p>' +
            '<p class="portfolio-item-info-content">' + Projects[i].teacher + '</p></div></div></div>'
        );

        $("#presentation-list").append(presentationItem(i, Projects[i]));
        if (i == 8) {
            $("#presentation-list").append('<tr><th scope="row"></th><td>15:00 - 15:20</td><td>中場休息</td><td></td><td></td></tr>');
        }
    }
}

function ShowProjectDialog(id) {
    var proj = Projects[id],
        html = ProjectItem(proj);
    $('#modal-body').html(html);
    $('#portfolioModal').modal('show');
    if (proj.img != null && proj.img.length > 1) {
        jssorSlidesInit();
    }
}

function ProjectItem(proj) {
    var html = '<h2>' + proj.title_zhtw + '</h2>' +
        '<hr class="star-primary">';

    if (proj.img != null && proj.img.length > 1) {
        html += jssorHtml(proj.id, proj.img);
    } else {
        html += '<img src="img/portfolio/' + (proj.img == null ? "no_photo.jpg" : proj.id + '/' + proj.img[0] + '.jpg') + '" class="img-responsive img-centered" alt="">';
    }

    html += '<br><p class="paragraph-indent paragraph-align">' + proj.introduction + '</p>' +
        '<ul class="list-inline item-details">' +
        '<li><p>學生:<strong>' + proj.student + '</strong></p></li>' +
        '<li><p>指導教授:<strong>' + proj.teacher + '</strong></p></li>' +
        '<li><p>海報:<strong><a href="' + (proj.poster == null ? "#" : proj.poster) + '" target="_blank">觀看海報</a></strong></p></li></ul>' +
        '<button type="button" class="btn btn-default" data-dismiss="modal"><i class="fa fa-times"></i> 關閉</button>';

    return html;
}

function jssorSlidesInit() {
    var jssor_1_options = {
        $AutoPlay: true,
        $ArrowNavigatorOptions: {
            $Class: $JssorArrowNavigator$
        },
        $ThumbnailNavigatorOptions: {
            $Class: $JssorThumbnailNavigator$,
            $Cols: 9,
            $SpacingX: 3,
            $SpacingY: 3,
            $Align: 260,
            $ChanceToShow: 1
        }
    };

    var jssor_1_slider = new $JssorSlider$("jssor-content", jssor_1_options);

    /*responsive code begin*/
    /*you can remove responsive code if you don't want the slider scales while window resizing*/
    var ScaleSlider = function() {
        var refSize = jssor_1_slider.$Elmt.parentNode.clientWidth;
        if (refSize) {
            refSize = Math.min(refSize, 800);
            jssor_1_slider.$ScaleWidth(refSize);
        } else {
            window.setTimeout(ScaleSlider, 30);
        }
    };

    ScaleSlider();
    $(window).bind("load", ScaleSlider);
    $(window).bind("resize", ScaleSlider);
    $(window).bind("orientationchange", ScaleSlider);
    /*responsive code end*/
}

function jssorHtml(id, img) {
    var html = '<div id="jssor-content" class="jssor-content-page"><div data-u="loading" class="jssor-content-loading"><div class="jssor-content-loading-background"></div>' +
        '<div class="jssor-content-loading-image"></div></div><div data-u="slides" class="jssor-content-slides">' +
        '<div data-p="112.50"><img data-u="image" height="300" src="img/portfolio/' + id + '/' + img[0] + '.jpg" /><img data-u="thumb" height="30" src="img/portfolio/' + id + '/' + img[0] + '.jpg" /></div>';

    for (var i = 1; i < img.length; i++) {
        html += '<div data-p="112.50" style="display: none;"><img data-u="image" height="300" src="img/portfolio/' + id + '/' + img[i] + '.jpg" /><img data-u="thumb" height="30" src="img/portfolio/' + id + '/thumb_' + img[i] + '.jpg" /></div>';
    }

    html += '</div><div data-u="thumbnavigator" class="jssor-content-thumbnavigator jssort03" data-autocenter="1"><div class="jssor-content-thumbnavigator-background"></div>' +
        '<div data-u="slides" class="jssor-content-thumbnavigator-slides"><div data-u="prototype" class="p"><div class="w"><div data-u="thumbnailtemplate" class="t"></div>' +
        '</div><div class="c"></div></div></div></div><span data-u="arrowleft" class="jssor-content-page-arrowleft jssora02l" data-autocenter="2"></span>' +
        '<span data-u="arrowright" class="jssor-content-page-arrowright jssora02r" data-autocenter="2"></span></div>';
    return html;
}

function presentationItem(i, proj) {
    return '<tr onclick="ShowProjectDialog(' + i + ');"><th scope="row">' + proj.id + '</th><td>' + proj.presentation_time + '</td><td>' + proj.title_zhtw + '</td><td>' + proj.teacher + '</td><td>' + proj.student + '</td></tr>';
}
