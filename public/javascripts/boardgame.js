const delay = millis => new Promise((resolve, reject) => {
    setTimeout(_ => resolve(), millis)
});

const rotateImage = function (calcRotation, dir) {
    let readIn = document.getElementById('cardP').getAttribute('rotation');
    let current_rotation = parseInt(readIn);
    var manicans = document.getElementsByName("manicans");

    for (let item of manicans) {
        item.style.visibility = "hidden";
    }

    var elem = document.getElementById("cardP");
    current_rotation = calcRotation(current_rotation);

    elem.style.transform = "rotate(" + current_rotation + "deg)";
    Promise.all(
        elem.getAnimations().map(
            function (animation) {
                return animation.finished
            }
        )
    ).then(
        function () {
            ajaxRotate(dir)
            return true;
        }
    )

    return false;
}

$(document).on('submit', 'form#rightButtonForm', (e) => {
    e.preventDefault();
    e.stopImmediatePropagation();
    rotateImage((rot) => { return rot + 90 }, "Right")
});

$(document).on('submit', 'form#leftButtonForm', (e) => {
    e.preventDefault();
    e.stopImmediatePropagation();
    rotateImage((rot) => { return rot - 90 }, "Left")
});

var ajaxRotate = function (dir) {
    let r = {
        "dir": dir
    };
    $.ajax({
        url: '/rotate',
        type: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(r),
        async: true,
        success: function (data) {
            $('div#freshCard').html(data);
        },
        error: function (e) {
            console.log(e);
        }
    });
    return false;
};

var ajaxPlaceCard = function (e) {
    e.preventDefault();
    e.stopImmediatePropagation();

    console.log( $( this ).attr( "id" ));
    let id = $( this ).attr( "id" );

    let request = {
        "row": parseInt($( this ).attr( "row" )),
        "col": parseInt($( this ).attr( "col" )),
    };

    $.ajax({
        url: '/placeCard',
        type: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(request),
        async: true,
        success: function (data) {
            $('form#' + id).html(data.tile);
            $('div#freshCard').html(data.freshCard);
            $('ul#stats').html(data.stats);
        },
        error: function (e) {
            console.log(e);
        }
    });
    return false;
};

$(document).on('submit', 'form.tile', ajaxPlaceCard);

var ajaxManican = function (e) {
    e.preventDefault();
    e.stopImmediatePropagation();

    console.log("manican " + $(this).attr("dir"));

    let request = {
        "dir": $(this).attr("dir")
    };

    $.ajax({
        url: '/selectArea',
        type: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(request),
        async: true,
        success: function (data) {
            $('div#freshCard').html(data);
        },
        error: function (e) {
            console.log(e);
        }
    });
    return false;
};

$(document).on('submit', 'form.manican', ajaxManican);