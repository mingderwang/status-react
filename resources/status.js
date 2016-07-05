var _status_catalog = {
    commands: {},
    responses: {}
};

function Command() {
}
function Response() {
}

Command.prototype.addToCatalog = function () {
    _status_catalog.commands[this.name] = this;
};

Command.prototype.param = function (parameter) {
    this.params.push(parameter);

    return this;
};

Command.prototype.create = function (com) {
    this.name = com.name;
    this.description = com.description;
    this.handler = com.handler;
    this.color = com.color;
    this.icon = com.icon;
    this.params = com.params || [];
    this.preview = com.preview;
    this.fullscreen = com.fullscreen;
    this.addToCatalog();

    return this;
};


Response.prototype = Object.create(Command.prototype);
Response.prototype.addToCatalog = function () {
    _status_catalog.responses[this.name] = this;
};
Response.prototype.onReceiveResponse = function (handler) {
    this.onReceive = handler;
};

function call(pathStr, paramsStr) {
    var params = JSON.parse(paramsStr),
        path = JSON.parse(pathStr),
        fn, res;

    fn = path.reduce(function (catalog, name) {
            if (catalog && catalog[name]) {
                return catalog[name];
            }
        },
        _status_catalog
    );

    if(!fn) {
        return null;
    }

    res = fn(params);

    return JSON.stringify(res);
}

function text(options, s) {
    return ['text', options, s];
}

function view(options, elements) {
    return ['view', options].concat(elements);
}

function image(options) {
    return ['image', options];
}

function touchable(options, element) {
    return ['touchable', options, element];
}

function scrollView(options, elements) {
    return ['scroll-view', options].concat(elements);
}

function webView(options, elements) {
    return ['web-view', options];
}

var status = {
    command: function (n, d, h) {
        var command = new Command();
        return command.create(n, d, h);
    },
    response: function (n, d, h) {
        var response = new Response();
        return response.create(n, d, h);
    },
    types: {
        TEXT: 'text',
        NUMBER: 'number',
        PHONE: 'phone',
        PASSWORD: 'password'
    },
    events: {
        SET_VALUE: 'set-value'
    },
    components: {
        view: view,
        text: text,
        image: image,
        touchable: touchable,
        scrollView: scrollView,
        webView: webView
    }
};
