function gameOver(json) {
    alert("Game Over!")
}

Vue.createApp({
    data() {
        return {
            username: "",
            connection: null,
            grid: null,
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
        },
        selectManican(dir) {
            this.connection.send(JSON.stringify({"manican": dir}))
        },
        rotateRight() {
            this.freshCard.rotation += 90;
            this.freshCard.manicansShow = false;
            this.connection.send(JSON.stringify({"rotate": "Right"}));
        },
        rotateLeft() {
            this.freshCard.rotation -= 90;
            this.freshCard.manicansShow = false;
            this.connection.send(JSON.stringify({"rotate": "Left"}))
        }
    },
    created() {
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
            this.connection = new WebSocket("ws://" + location.hostname + ":9000/websocket");
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
                        this.players = value.players;
                    }
                    if (key === 'freshCard') {
                        this.freshCard = value
                    }
                    if (key === 'grid') {
                        console.log(value);
                        this.grid = value
                    }
                }
            }
        };
    },
    computed: {
        rotationFreshCard() {
            return {
                transform: 'rotate(' + this.freshCard.rotation + 'deg)',
                transition: 'all 0.2s'
            }
        },
    }
})
    .component('the-player-stats', {
        template: '#the-player-stats-template',
        props: {},
    })
    .mount('#app')
