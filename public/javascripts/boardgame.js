let websocket;
$(document).ready(function () {
    websocket = new WebSocket("ws://" + location.hostname + ":9000/websocket");
    connectWebSocket(handle)
})
const connectWebSocket = handleData => {
    websocket.onopen = (event) => {
        console.log("Connected to Websocket");
        let msg = {
            "connect": "success"
        }
        websocket.send(JSON.stringify(msg));
    };

    websocket.onclose = () => {
        console.log("Connection with Websocket Closed!");
    };

    websocket.onerror = (error) => {
        console.log("Error in Websocket Occured: " + error);
        websocket = new WebSocket("ws://" + location.hostname + ":9000/websocket");
        connectWebSocket(handle)
    };

    websocket.onmessage = (e) => {
        console.log("received message " + e)
        if (typeof e.data === "string") {
            handleData(e.data)
        }
    };
};


function handle(rawData) {
    const json = JSON.parse(rawData);

    const r = json['refresh'];
    if (r != null) {
        refresh(r);
    }
    const g = json['gameOver'];
    if (g != null) {
        gameOver();
    }
}

function gameOver(json) {
    alert("Game Over!")
}

function refresh(json) {
    console.log("refreshing...")
    $('form#' + json.tileId).html(json.tile);
    $('div#freshCard').html(json.freshCard);
    $('ul#stats').html(json.stats);
}

const delay = millis => new Promise((resolve, reject) => {
    setTimeout(_ => resolve(), millis)
});

const ajaxRotate = function (dir) {
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
    rotateImage((rot) => {
        return rot + 90
    }, "Right")
});

$(document).on('submit', 'form#leftButtonForm', (e) => {
    e.preventDefault();
    e.stopImmediatePropagation();
    rotateImage((rot) => {
        return rot - 90
    }, "Left")
});

const ajaxPlaceCard = function (e) {
    e.preventDefault();
    e.stopImmediatePropagation();

    console.log($(this).attr("id"));

    let request = {
        "refresh": {
            "row": parseInt($(this).attr("row")),
            "col": parseInt($(this).attr("col")),
        }
    };
    websocket.send(JSON.stringify(request));
    return false;
};

$(document).on('submit', 'form.tile', ajaxPlaceCard);

const ajaxManican = function (e) {
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

Vue.createApp({
    data() {
        return {
            username: "",
            connection: null,
            tiles: null,
            freshCard: null,
            players: [],
        }
    },
    methods: {
        placeCard(row, col) {
            let request = {
                "refresh": {
                    "row": row,
                    "col": col,
                }
            };
            this.connection.send(JSON.stringify(request));
        }
    },
    async mounted() {

        console.log("Connecting to WebSocket...");
        this.connection = new WebSocket("ws://" + location.hostname + ":9000/websocket");

        this.connection.onopen = (event) => {
            console.log("Connected to Websocket");
            let msg = {
                "connect": "success"
            }
            this.connection.send(JSON.stringify(msg));
        };

        this.connection.onclose = () => {
            console.log("Connection with Websocket Closed!");
        };

        this.connection.onerror = (error) => {
            console.log("Error in Websocket Occured: " + error);
            // this.connection = new WebSocket("ws://" + location.hostname + ":9000/websocket");
        };

        this.connection.onmessage = (e) => {
            console.log("received message " + e)

            if (typeof e.data === "string") {
                let json = JSON.parse(e.data)

                for (let [key, value] of Object.entries(json)) {
                    console.log(`Received ${key}: ${value}`)
                    if (key === 'username') {
                        this.username = value;
                    }
                    if (key === 'stats') {
                        console.log(value)
                        this.players = value.players;
                    }
                }
            }
        };
    }
})
    .component('the-player-stats', {
        template: '#the-player-stats-template',
        props: {},
    })
    .mount('#app')
