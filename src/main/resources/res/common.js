/**
 * Common functions
 * =================
 */

$.fn.serializeObject = function() {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

function populate(form, data) {
    $.each(data, function (key, value) {
        var $ctrl = $('[name=' + key + ']', form);
        switch ($ctrl.prop("type")) {
            case "radio":
            case "checkbox":
                $ctrl.prop('checked', '').each(function() {
                    var $input = $(this);

                    if (!$.isArray(value)) {
                        value = [value];
                    }

                    $.each(value, function (index, val) {
                        if ($input.attr('value') == val) {
                            $input.prop('checked', 'checked');
                        }
                    });
                });
                break;
            default:
                $ctrl.val(value);
        }
    });
}

function handleDialog(text) {
    var dialog = $('#dialog');

    if (dialog.size() == 0) {
        var element = document.createElement('div');
        $('body').append(element);
        dialog = $(element);
    }

    dialog.html(text);
    if (dialog.is(":data( 'dialog' )")) {
        dialog.dialog('destroy');
    }

    return dialog;
}

function openDialog(text, title, options) {
    if (options === undefined) options = {};

    var position = { my: "center center", at: "center center", of: window }
    var previous = $('.dialog:visible:last');
    if (previous.length) {
        position = { my: "center top", at: "center+50 top+50", of: previous.closest('.ui-dialog') };
    }

    return handleDialog(text).dialog($.extend({
        title: title,
        width: 1000,
        height: 600,
        resizable: true,
        position: position,
        buttons: {
            "Close": function() {
                $(this).dialog('close');
            }
        },
        close: function () {
            $(this).dialog('destroy').remove();
        }
    }, options));
}

function openAlert(text, title, options) {
    return openDialog(text, title, $.extend({
        width: 480,
        height: 200,
        modal: true,
        resizable: false
    }, options));
}

function closeDialog(className) {
    $('.dialog' + (className ? '-' + className : '')).each(function () {
        var $dialog = $(this).closest('.ui-dialog-content');
        if ($dialog.length) {
            $dialog.dialog('close');
        }
    });
}

function getMatches(string, regex, index) {
    index || (index = 1);

    var matches = [];
    var match;

    while (match = regex.exec(string)) {
        matches.push(match[index]);
    }

    return matches;
}

Handlebars.registerHelper('breaklines', function(text) {
    text = Handlebars.Utils.escapeExpression(text);
    text = text.replace(/(\r\n|\n|\r)/gm, '<br>');
    return new Handlebars.SafeString(text);
});

Handlebars.registerHelper('json', function(context) {
    return JSON.stringify(context);
});

Handlebars.registerHelper("inc", function(value, options) {
    return parseInt(value) + 1;
});

Handlebars.registerHelper('raw', function(text) {
    return new Handlebars.SafeString(text);
});

Handlebars.registerHelper('ifCond', function (v1, operator, v2, options) {
    switch (operator) {
        case '==':
            return (v1 == v2) ? options.fn(this) : options.inverse(this);
        case '===':
            return (v1 === v2) ? options.fn(this) : options.inverse(this);
        case '!=':
            return (v1 != v2) ? options.fn(this) : options.inverse(this);
        case '!==':
            return (v1 !== v2) ? options.fn(this) : options.inverse(this);
        case '<':
            return (v1 < v2) ? options.fn(this) : options.inverse(this);
        case '<=':
            return (v1 <= v2) ? options.fn(this) : options.inverse(this);
        case '>':
            return (v1 > v2) ? options.fn(this) : options.inverse(this);
        case '>=':
            return (v1 >= v2) ? options.fn(this) : options.inverse(this);
        case '&&':
            return (v1 && v2) ? options.fn(this) : options.inverse(this);
        case '||':
            return (v1 || v2) ? options.fn(this) : options.inverse(this);
        default:
            return options.inverse(this);
    }
});

/**
 * Common logic
 * ============
 */
$(function () {

    // Disable default popup when ajax call is aborted
    $(document).off('ajaxError');

    // Legal notice
    $('#legal-notice-template').each(function () {
        var $template = $(this);

        var cookieName = 'felix-webconsole-search';
        if (!$.cookie(cookieName)) {
            var legalNoticeTemplate = Handlebars.compile($template.html());
            openDialog(legalNoticeTemplate(), 'Legal notice', {
                width: 640,
                height: 480,
                modal: true,
                buttons: {
                    "Accept": function() {
                        $(this).dialog('close');
                        $.cookie(cookieName, true, { expires: 365 })
                    },
                    "Decline": function() {
                        location.href = appRoot;
                    }
                }
            });
        }
    });

});


