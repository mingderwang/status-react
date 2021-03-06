var _status_catalog = {
        commands: {},
        responses: {}
    },
    status = {};

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
    this["has-handler"] = com.handler != null;
    this.validator = com.validator;
    this.color = com.color;
    this.icon = com.icon;
    this.params = com.params || [];
    this.preview = com.preview;
    this["suggestions-trigger"] = com.suggestionsTrigger || "on-change";
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

var context = {}

function addContext(ns, key, value) {
    context[ns][key] = value;
}

function call(pathStr, paramsStr) {
    var params = JSON.parse(paramsStr),
        path = JSON.parse(pathStr),
        fn, callResult, message_id;

    if (typeof params.context !== "undefined" &&
        typeof params.context["message-id"] !== "undefined") {
        message_id = params.context["message-id"];
    } else {
        message_id = null;
    }
    context[message_id] = {};
    status.message_id = message_id;

    fn = path.reduce(function (catalog, name) {
            if (catalog && catalog[name]) {
                return catalog[name];
            }
        },
        _status_catalog
    );

    if (!fn) {
        return null;
    }

    callResult = fn(params.parameters, params.context);
    result = {
        returned: callResult,
        context: context[message_id]
    };

    return JSON.stringify(result);
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

function webView(url) {
    return ['web-view', {
        source: {
            uri: url
        },
        javaScriptEnabled: true
    }];
}

function validationMessage(titleText, descriptionText) {
    var titleStyle = {
        style: {
            color: "white",
            fontSize: 12
        }
    };
    var title = status.components.text(titleStyle, titleText);

    var descriptionStyle = {
        style: {
            color: "white",
            fontSize: 12,
            opacity: 0.9
        }
    };
    var description = status.components.text(descriptionStyle, descriptionText);

    return status.components.view(
        {
            backgroundColor: "red",
            height: 61,
            paddingLeft: 16,
            paddingTop: 14,
        },
        [title, description]
    );
}

var status = {
    command: function (h) {
        var command = new Command();
        return command.create(h);
    },
    response: function (h) {
        var response = new Response();
        return response.create(h);
    },
    autorun: function (commandName) {
        _status_catalog.autorun = commandName;
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
        webView: webView,
        validationMessage: validationMessage
    }
};
