
function SatoriViewModel() {
    var self = this;
    self.loginDialog = ko.observable(null);
    self.contests = ko.observable(null);
    self.contest = ko.observable(null);
    self.subpage = ko.observable(null);
    self.news = ko.observable(null);
    self.globalNews = ko.observable(null);
    self.results = ko.observable(null);
    self.problems = ko.observable(null);
    self.result = ko.observable(null);

    self.clear = function() {
        epochId ++;
        self.loginDialog(null);
        self.contests(null);
        self.contest(null);
        self.subpage(null);
        self.news(null);
        self.results(null);
        self.problems(null);
        self.result(null);
    };

    self.currentSubpageId = function() {
        return self.subpage() && self.subpage().id;
    };

    var app = Sammy(function () {
        this.before({}, function() {
            self.clear();
        })

        this.get('#/login', function () {
            self.loginDialog({
                login: function() {
                    location.hash = '#/contests';
                }
            });
        });

        function loadContest(req, callback) {
            getCached(RARE, '/page-info/' + req.params.id, function(result) {
                $.each(result.subpages, function(i, value) {
                    value.gotoPage = function() {
                        location.hash = '#/contest/' + req.params.id + '/subpage/' + value.id;
                    };
                });
                result.gotoNews = function() {
                    location.hash = '#/contest/' + req.params.id;
                };
                result.gotoResults = function() {
                    location.hash = '#/contest/' + req.params.id + '/results';
                };
                result.gotoProblems = function() {
                    location.hash = '#/contest/' + req.params.id + '/problems';
                };

                callback(result);
                self.contest(result);
            });
        }

        this.get('#/contest/:id', function() {
            var req = this;
            getCached(RARE, '/news/' + req.params.id, function(result) {
                self.news(result);
            });

            loadContest(this, function(result) {

            });
        });

        this.get('#/contest/:id/result/:submit', function() {
            var req = this;
            var startEpoch = epochId;
            loadContest(this, function(result) {
                if(startEpoch != epochId) {
                    clearInterval(intervalId);
                }
                getCached(RARE, '/result/' + req.params.submit, function(result) {
                    self.result(result);
                }, startEpoch);
            });
        });

        this.get('#/contest/:id/results', function() {
            var req = this;
            var startEpoch = epochId;
            loadContest(this, function(result) {
                function load(first) {
                    if(startEpoch != epochId) {
                        clearInterval(intervalId);
                    }
                    getCached(first ? null : REFRESH, '/results/' + req.params.id, function(results) {
                        $.each(results, function(i, result) {
                            result.openDetailed = function() {
                                result.showDetailed(!result.showDetailed());
                            };
                            result.showDetailed = ko.observable(i === 0);
                        });
                        console.log('render results');
                        self.results(results);
                    }, startEpoch);
                }

                var intervalId = setInterval(load, 5000);
                load(true);
            });
        });

        function doSubmit(problem) {

        }

        this.get('#/contest/:id/problem/:problem', function() {
            var req = this;
            var startEpoch = epochId;
            loadContest(this, function(result) {
                getCached(RARE, '/problems/' + req.params.id, function(problems) {
                    $.each(problems, function(i, problem) {
                        if(problem.id == req.param('problem')) {

                        }
                    });
                });
            });
        });

        this.get('#/contest/:id/problems', function() {
            var req = this;
            var startEpoch = epochId;
            loadContest(this, function(result) {
                getCached(RARE, '/problems/' + req.params.id, function(problems) {
                    $.each(problems, function(i, problem) {
                        problem.status = null;
                        problem.doSubmit = function() {
                            doSubmit(problem);
                        };
                        problem.href = '/blob/ProblemMapping/' +
                            problem.problem_mapping.id + '/statement_files/_pdf/' + problem.problem_mapping.code + '.pdf';
                        problem.html_href = '#/contest/:id/problem/:problem';
                    });
                    self.problems(problems);

                    getCached(null, '/results/' + req.params.id, function(results) {
                        $.each(problems, function(i, problem) {
                            for(var j=results.length - 1; j >= 0; j --) {
                                var result = results[j];
                                if(result.problem_mapping.id == problem.problem_mapping.id) {
                                    problem.status = result.status;
                                    if(problem.status == 'OK') break;
                                }
                            }
                        });

                        self.problems(null); // force update
                        self.problems(problems);
                    }, startEpoch);
                }, startEpoch);
            });
        });

        this.get('#/contest/:id/subpage/:subpage', function() {
            var req = this;
            loadContest(req, function(result) {
                $.each(result.subpages, function(i, value) {
                    if(value.id === parseInt(req.params.subpage)) {
                        console.log(value);
                        value.content = rebaseHTML(value.content,
                                                   '/blob/Subpage/' +
                                                   value.id + '/content_files/{}/{}');
                        self.subpage(value);
                    }
                });
            });
        });

        this.get('#/contests', function () {
            getCached(RARE, '/contests', function(result) {
                var mainContests = [];
                var otherContests = [];
                var archivedContests = [];
                for(var i in result) {
                (function(i) {
                    var item = result[i];
                    var accepted = item.contestant && item.contestant.accepted;
                    (accepted ? (item.contest.archived ? archivedContests : mainContests) : otherContests)
                        .push({
                            name: item.contest.name,
                            id: item.contest.id,
                            description: item.contest.description,
                            open: function() {
                                location.hash = '#/contest/' + item.contest.id;
                            }
                        });
                })(i);
                 }
                self.contests([
                    {name: 'My contests', contests: mainContests, news: true},
                    {name: 'My archived contests', contests: archivedContests, news: false},
                    {name: 'Other contests', contests: otherContests, news: true},
                ]);
            });

            getCached(null, '/global-news', function(result) {
                self.globalNews(result);
            });
        });

        this.get('/', function () {
            location.hash = '#/contests';
        });

        this._checkFormSubmission = function(form) {
            return false;
        };
    });
    app.raise_error = true;
    app.run();
}

function rebaseHTML(data, baseHref) {
    var elem = $('<div>' + data + '</div>');
    elem.find('[href]').each(function() {
        var self = $(this);
        if(self.attr('href').indexOf('://') == -1)
            self.attr('href', baseHref.replace(/{}/g, self.attr('href')));
        self.attr('target', '_blank');
    });
    return elem.html();
}

var RARE = 'rare';
var REFRESH = 'refresh';
var CACHE = {};
var epochId = 1;
var animRef = 0;

function startAnim(type) {
    if(type == REFRESH) return;
    animRef ++;
    if(animRef == 1) {
        $('.navbar').animate({'background-color': '#ddd'}, {queue: false});
        $('.loading').show();
    }
}

function finishAnim(type) {
    if(type == REFRESH) return;
    animRef --;
    if(animRef === 0) {
        $('.navbar').animate({'background-color': '#fff'}, {queue: false});
        $('.loading').hide();
    }
}

var cacheStorage = localStorage;

function getCached(type, url, callback, startEpoch) {
    if(!startEpoch)
        startEpoch = epochId;
    if(startEpoch != epochId)
        return;

    startAnim(type);

    var finished = false;

    if(type != REFRESH) {
        var cachedVal = cacheStorage.getItem(url);
        if(cachedVal) {
            callback(JSON.parse(cachedVal));
            if(type == RARE) {
                finished = true;
                finishAnim(type);
                return;
            }
        }
    }

    $.ajax({
        url: url,
        success: function(data) {
            if(startEpoch != epochId) return finished || finishAnim(type);
            var dataString = JSON.stringify(data);
            var cachedVal = cacheStorage.getItem(url);
            if(!cachedVal || cachedVal != dataString) {
                cacheStorage.setItem(url, dataString);
                callback(data);
            }
            return finished || finishAnim(type);
        },
        error: function() {
            return finished || finishAnim(type);
        }
    });
}

var vm
ko.applyBindings(vm = new SatoriViewModel());
