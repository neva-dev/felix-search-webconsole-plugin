$(function () {

    /**
     * Search plugin logic
     * ===================
     */

    var $body = $("body");

    // Search form
    var $form = $('#search-form'),
        $results = $('#search-results'),
        resultsTemplate = Handlebars.compile($('script[type="text/x-handlebars-template"]', $results).html()),
        $phraseInput = $('input[name=phrase]', $form),
        $filterInput = $('input[name=provider]', $form),
        $cachedInput = $('select[name=cached]', $form),
        $resultLimitInput = $('input[name=resultLimit]', $form),
        $progressSpinner = $('.progress .spinner', $form),
        $progressText = $('.progress .text', $form),
        searchXhr = null;

    function search() {
    if (!$phraseInput.val().length) {
        return;
    }

    if (searchXhr) {
        searchXhr.abort();
    }

    searchXhr = $.ajax({
        url: pluginRoot + '/by-phrase',
        type: 'GET',
        data: $form.serializeArray(),
        beforeSend: function () {
            $results.empty();
            $progressText.text('Please wait...');
            $progressSpinner.removeClass('done');

            if (history.pushState) {
                var formParams = $form.serializeObject();
                var uri = new URI().search(formParams).toString();
                history.pushState({}, document.title, uri)
            }
        },
        complete: function () {
            $progressSpinner.addClass('done');
            $progressText.text('Completed');
        },
        success: function (response) {
            var vars = $.extend({
                url: this.url
            }, response.data);

            $results.data(vars);
            $results.html(resultsTemplate(vars));
        },
        error: function (xhr) {
            if (xhr.statusText != 'abort') {
                openAlert('Cannot perform search by phrase. Internal server error.');
            }
        }
    });
    };

    var formParams = new URI().search(true);
    populate($form, formParams);

    $phraseInput.on('keyup', _.debounce(search, 1000));
    $filterInput.on('change', search);
    $cachedInput.on('change', search);
    $resultLimitInput.on('change', search);

    search();

    $body.delegate('.class-decompile', 'click', function () {
        var $link = $(this);

        classDecompile($link.attr('href'));

        return false;
    });

    // Viewer for decompiled class source
    var classDecompileTemplate = Handlebars.compile($('#class-decompile-template').html());
    function classDecompile(url) {
        $.ajax({
            url: url,
            type: 'GET',
            success: function (response) {
                var html = classDecompileTemplate(response.data);
                var title = response.data.className + " (" + response.data.bundleId + ")";

                closeDialog('class-bundle');
                openDialog(html, title);
                classSourcePrettyPrint();
            },
            error: function () {
                openAlert('Cannot decompile class. Internal server error.');
            }
        });
    }

    // Chooser for classes found in more than one bundle
    var classBundleTemplate = Handlebars.compile($('#class-bundle-template').html());
    function classSourcePrettyPrint() {
        $('.class-source').each(function () {
            var $source = $(this);

            if ($source.hasClass('prettyprinted')) {
                return;
            } else {
                $source.addClass('prettyprinted');
            }

            var classSource = $source.html();
            var highlightedSource = PR.prettyPrintOne(classSource, 'lang-java' /*, 'line-nums'*/);

            $source.data('source', classSource);
            $source.html(highlightedSource);

            var package = getMatches(classSource, /package (.*);/gi, 1)[0];
            var imports = _.uniq(getMatches(classSource, /import (.*);/gi, 1));

            $('span.typ', $source).each(function () {
                var $link = $(this);
                var type = $(this).text();
                var className = package + "." + type;

                for (var i = 0; i < imports.length; i++) {
                    if (_.last(imports[i].split('.')) == type) {
                        className = imports[i];
                        break;
                    }
                }

                $link.attr({
                    title: 'Click to view source of this type.',
                });

                $link.on('click', function () {
                    $.ajax({
                        type: 'GET',
                        url: pluginRoot + '/by-phrase',
                        data: {
                            phrase: className,
                            provider: 'class',
                            cached: false
                        },
                        success: function (response) {
                            var results = _.filter(response.data.results, function (result) {
                                return result.context.className == className;
                            });

                            if (!results.length) {
                                openAlert('Class cannot be found. It is probably class available only at compile time (e.g. part of JDK) or private inner class.');
                            } else if (results.length == 1) {
                                classDecompile(_.first(results).context.classDecompileUrl)
                            } else {
                                openDialog(classBundleTemplate({
                                    className,
                                    results: results
                                }), "Choose appropriate class");
                            }
                        },
                        error: function () {
                            openAlert('Cannot find referenced class. Internal server error.');
                        },
                    });

                    return false;
                });
            });
        });
    }

    // Viewer for classes in concrete bundle
    var bundleClassesTemplate = Handlebars.compile($('#bundle-classes-template').html());
    $body.delegate('.bundle-classes', 'click', function () {
        var $link = $(this);

        $.ajax({
            url: $link.attr('href'),
            type: 'GET',
            success: function (response) {
                var html = bundleClassesTemplate(response);
                var title = response.bundleSymbolicName + " (" + response.bundleId + ")";

                var $dialog = openDialog(html, title);
                var $tree = $dialog.find('.tree')

                $tree.tree({
                    data: response.classes.tree,
                    autoOpen: response.classes.count < 200 ? true : 2
                }).bind('tree.click', function (e) {
                    var node = e.node;

                    if (node.decompileUrl) {
                        classDecompile(node.decompileUrl);
                    }
                });
            },
            error: function () {
                openAlert('Cannot compose bundle class tree. Internal server error.');
            }
        });

        return false;
    });

    // Search for text in class sources from concrete bundle
    $body.delegate('.select-all', 'click', function () {
        $('.result', $results).each(function () {
            $('[name=result]', this).prop('checked', 'checked');
        });
    });

    $body.delegate('.deselect-all', 'click', function () {
        $('.result', $results).each(function () {
            $('[name=result]', this).prop('checked', '');
        });
    });

    function getSelectedResults() {
        var $results = $('.result', $results).filter(function () {
            return $('[name=result]', this).prop('checked');
        });

        return $results.map(function () {
            var $result = $(this);
            var config = $result.data('result');

            return config;
        }).get();
    }

    function getSelectedBundleData() {
        var data = {
            bundleIds: [],
            bundleClasses: []
        };

        _.each(getSelectedResults(), function (result) {
            var ctx = result.context;

            if (ctx.bundleId && ctx.className) {
                data.bundleClasses.push(ctx.bundleId + ',' + ctx.className);
            } else if (ctx.bundleId) {
                data.bundleIds.push(ctx.bundleId);
            }
        });

        data.total = data.bundleIds.length + data.bundleClasses.length;

        return data;
    }

    // Search classes form
    (function () {
        var searchClassesResultsTemplate = Handlebars.compile($('#search-classes-results-template').html());
        var jobCurrent = null;
        var jobPoll = null;

        function start() {
            if (jobPoll != null) {
                return;
            }

            var $form = $('.dialog-search-classes');
            var $progressSpinner = $('.progress .spinner', $form);
            var $progressText = $('.progress .text', $form);
            var $startButton = $('.class-decompile-start', $form);
            var $stopButton = $('.class-decompile-stop', $form);

            var bundleData = getSelectedBundleData();
            var $results = $('#search-classes-results');

            $.ajax({
                type: 'POST',
                url: pluginRoot + '/class-search',
                data: {
                    phrase: $('input[name=text]', $form).val(),
                    bundleId: bundleData.bundleIds,
                    bundleClass: bundleData.bundleClasses
                },
                beforeSend: function () {
                    $results.empty();
                    $progressText.text('Please wait...');
                    $progressSpinner.removeClass('done');
                },
                success: function (response) {
                    var job = response.data;

                    $stopButton.show();
                    $startButton.hide();

                    jobCurrent = job;
                    jobPoll = setInterval(function () {
                        $.ajax({
                            type: 'GET',
                            url: pluginRoot + '/class-search?jobId=' + job.id,
                            success: function (response) {
                                var job = response.data;

                                // Append partial results
                                if (job.partialResults.length) {
                                    var $result = $(searchClassesResultsTemplate(job)).tabs();
                                    $results.append($result);
                                    classSourcePrettyPrint();
                                }

                                if (job.progress == 100) {
                                    stop();
                                } else {
                                    // Display percentage only it total is calculated
                                    if (job.total <= 0) {
                                        $progressText.text(job.step);
                                    } else {
                                        $progressText.text(job.step + ' ' + job.progress.toFixed(2) + '% (' + job.count + ' / ' + job.total + ')');
                                    }
                                }
                            },
                            error: function () {
                                openAlert('Cannot poll class search. Internal server error.');
                            },
                        });
                    }, 1000);
                },
                error: function () {
                    openAlert('Cannot start class search. Internal server error.');
                },
            });
        }

        function stop() {
            if (jobCurrent == null || jobPoll == null) {
                return;
            }

            var $form = $('.dialog-search-classes');
            var $progressSpinner = $('.progress .spinner', $form);
            var $progressText = $('.progress .text', $form);
            var $startButton = $('.class-decompile-start', $form);
            var $stopButton = $('.class-decompile-stop', $form);

            $.ajax({
                type: 'DELETE',
                url: pluginRoot + '/class-search?jobId=' + jobCurrent.id,
                success: function (response) {
                    var job = response.data;

                    clearInterval(jobPoll);
                    $progressSpinner.addClass('done');
                    $progressText.text('Completed');
                    $stopButton.hide();
                    $startButton.show();

                    jobPoll = null;
                    jobCurrent = null;
                },
                error: function () {
                    openAlert('Cannot stop class search. Internal server error.');
                }
            });

            return false;
        }

        var searchClassesTemplate = Handlebars.compile($('#search-classes-template').html());
        $body.delegate('.search-classes', 'click', function () {
            var results = getSelectedResults();
            if (!results.length) {
                openAlert("Please select elements in which classes will be searched.", "Error");
                return;
            }

            var elements = results.length == 1 ? ("'" + results[0].label + "'") : results.length + " elements";
            openDialog(searchClassesTemplate(), "Decompile classes & search in " + elements, {
                modal: true,
                close: function () {
                    stop();
                }
            });
        });

        $body.delegate('.class-decompile-start', 'click', function () {
            start();

            return false;
        });

        $body.delegate('.class-decompile-stop', 'click', function () {
            stop();
            return false;
        });

        $body.delegate('.dialog-search-classes form', 'submit', function () {
            start();
        });
    }());

    // Generate sources
   (function () {
        var resultsTemplate = Handlebars.compile($('#source-generate-results-template').html());
        var jobCurrent = null;
        var jobPoll = null;

        function start() {
            if (jobPoll != null) {
                return;
            }

            var $form = $('.dialog-source-generate');
            var $progressSpinner = $('.progress .spinner', $form);
            var $progressText = $('.progress .text', $form);
            var $startButton = $('.source-generate-start', $form);
            var $stopButton = $('.source-generate-stop', $form);

            var bundleData = getSelectedBundleData();
            var $results = $('#source-generate-results');

            $.ajax({
                type: 'POST',
                url: pluginRoot + '/source-generate',
                data: {
                    bundleId: bundleData.bundleIds,
                    bundleClass: bundleData.bundleClasses
                },
                beforeSend: function () {
                    $results.empty();
                    $progressText.text('Please wait...');
                    $progressSpinner.removeClass('done');
                },
                success: function (response) {
                    var job = response.data;

                    $stopButton.show();
                    $startButton.hide();

                    jobCurrent = job;
                    jobPoll = setInterval(function () {
                        $.ajax({
                            type: 'GET',
                            url: pluginRoot + '/source-generate?jobId=' + job.id,
                            success: function (response) {
                                var job = response.data;

                                if (job.progress == 100) {
                                    stop();

                                    $results.html(resultsTemplate(job));
                                } else {
                                    // Display percentage only it total is calculated
                                    if (job.total <= 0) {
                                        $progressText.text(job.step);
                                    } else {
                                        $progressText.text(job.step + ' ' + job.progress.toFixed(2) + '% (' + job.count + ' / ' + job.total + ')');
                                    }
                                }
                            },
                            error: function () {
                                openAlert('Cannot poll source generation. Internal server error.');
                            },
                        });
                    }, 1000);
                },
                error: function () {
                    openAlert('Cannot start source generation. Internal server error.');
                },
            });
        }

        function stop() {
            if (jobCurrent == null || jobPoll == null) {
                return;
            }

            var $form = $('.dialog-source-generate');
            var $progressSpinner = $('.progress .spinner', $form);
            var $progressText = $('.progress .text', $form);
            var $startButton = $('.source-generate-start', $form);
            var $stopButton = $('.source-generate-stop', $form);

            $.ajax({
                type: 'DELETE',
                url: pluginRoot + '/source-generate?jobId=' + jobCurrent.id,
                success: function (response) {
                    var job = response.data;

                    clearInterval(jobPoll);
                    $progressSpinner.addClass('done');
                    $progressText.text('Completed');
                    $stopButton.hide();
                    $startButton.show();

                    jobPoll = null;
                    jobCurrent = null;
                },
                error: function () {
                    openAlert('Cannot stop source generation. Internal server error.');
                }
            });

            return false;
        }

        var generateSourcesTemplate = Handlebars.compile($('#source-generate-template').html());
        $body.delegate('.source-generate', 'click', function () {
            var results = getSelectedResults();
            if (!results.length) {
                openAlert("Please select elements for which sources will be generated.", "Error");
                return;
            }

            var elements = results.length == 1 ? ("'" + results[0].label + "'") : results.length + " elements";
            openDialog(generateSourcesTemplate(), "Generate sources for " + elements, {
                modal: true,
                width: 480,
                height: 200,
                close: function () {
                    stop();
                }
            });

            start();
        });

        $body.delegate('.source-generate-start', 'click', function () {
            start();

            return false;
        });

        $body.delegate('.source-generate-stop', 'click', function () {
            stop();

            return false;
        });

        $body.delegate('.dialog-source-generate form', 'submit', function () {
            start();
        });
    }());
});
