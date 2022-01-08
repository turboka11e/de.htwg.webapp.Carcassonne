Vue.createApp({
    data() {
        return {
            username: "",
            connection: new WebSocket("wss://" + location.hostname + "/websocket"),
            grid: null,
            freshCard: null,
            players: [],
            chat: [],
            message: "",
            readyState: 3,
            gameOver: false,
            activeUser: false,
        }
    },
    methods: {
        newWebsocket() {
            console.log("Connecting to WebSocket...");
            if (this.connection.readyState === WebSocket.CLOSED) {
                this.connection = new WebSocket("wss://" + location.hostname + "/websocket");
            }
            this.connection.onopen = (event) => {
                this.readyState = this.connection.readyState
                console.log("Connected to Websocket");
                let msg = {
                    "connect": "success"
                }
                this.connection.send(JSON.stringify(msg));
            };
            this.connection.onclose = () => {
                this.readyState = this.connection.readyState
                console.log("Connection with Websocket Closed!");
            };

            this.connection.onerror = (error) => {
                this.readyState = this.connection.readyState
                console.log("Error in Websocket Occured: " + error);
                this.newWebsocket()
            };

            this.connection.onmessage = (e) => {
                this.readyState = this.connection.readyState
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
                            this.grid = value;
                        }
                        if (key === 'gameOver') {
                            this.gameOver = true;
                        }
                        if (key === 'chat') {
                            console.log(value);
                            this.chat = value;
                        }
                        if (key === 'activeUser') {
                            this.activeUser = value;
                        }
                    }
                }
            };
        },
        sendMessage() {
            if (!(this.message.trim() === '')) {
                this.connection.send(JSON.stringify({
                    "chat": {
                        "username": this.username,
                        "msg": this.message,
                        "timestamp": new Date().toLocaleTimeString('en-US', {hour12: false}),
                    }
                }))
                this.message = ''
            }
        },
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
            this.freshCard.manicansShow = false;
            this.connection.send(JSON.stringify({"rotate": "Right"}));
        },
        rotateLeft() {
            this.freshCard.manicansShow = false;
            this.connection.send(JSON.stringify({"rotate": "Left"}))
        }
    },
    created() {
        this.newWebsocket()
    },
    computed: {
        rotationFreshCard() {
            return {
                transform: 'rotate(' + this.freshCard.rotation + 'deg)',
                transition: 'all 0.2s'
            }
        },
        websocketState() {
            switch (this.readyState) {
                case 0:
                    return "CONNECTING"
                case 1:
                    return "OPEN"
                case 2:
                    return "CLOSING"
                case 3:
                    return "CLOSED"
            }
        },
        websocketStateColor() {
            switch (this.readyState) {
                case 0:
                    return "bg-success"
                case 1:
                    return "bg-success"
                case 2:
                    return "bg-danger"
                case 3:
                    return "bg-danger"
            }
        },
    }
})
    .component('the-player-stats', {
        template: '#the-player-stats-template',
        props: {},
    })
    .mount('#app')
