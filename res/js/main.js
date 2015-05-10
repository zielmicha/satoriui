
function SatoriViewModel() {
    var self = this;
    self.loginDialog = ko.observable(null);
    self.contests = ko.observable(null);
    self.contest = ko.observable(null);
    self.subpage = ko.observable(null);
    self.news = ko.observable(null);
    self.results = ko.observable(null);
    self.problems = ko.observable(null);

    self.clear = function() {
        epochId ++;
        self.loginDialog(null);
        self.contests(null);
        self.contest(null);
        self.subpage(null);
        self.news(null);
        self.results(null);
        self.problems(null);
    }

    self.currentSubpageId = function() {
        return self.subpage() && self.subpage().id;
    }

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
                }
                result.gotoResults = function() {
                    location.hash = '#/contest/' + req.params.id + '/results';
                }
                result.gotoProblems = function() {
                    location.hash = '#/contest/' + req.params.id + '/problems';
                }

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
                            }
                            result.showDetailed = ko.observable(i == 0);
                        });
                        console.log('render results');
                        self.results(results);
                    }, startEpoch)
                }

                var intervalId = setInterval(load, 5000);
                load(true);
            });
        });

        function doSubmit(problem) {

        }

        this.get('#/contest/:id/problems', function() {
            var req = this;
            var startEpoch = epochId;
            loadContest(this, function(result) {
                getCached(RARE, '/problems/' + req.params.id, function(problems) {
                    $.each(problems, function(i, problem) {
                        problem.status = null;
                        problem.doSubmit = function() {
                            doSubmit(problem)
                        }
                        problem.href = 'https://satori.tcs.uj.edu.pl/view/ProblemMapping/' +
                            problem.problem_mapping.id + '/statement_files/_pdf/' + problem.problem_mapping.code + '.pdf'
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
                    if(value.id === parseInt(req.params.subpage))
                        self.subpage(value);
                });
            });
        });

        this.get('#/contests', function () {
            getCached(RARE, '/contests', function(result) {
                var mainContests = []
                var otherContests = []
                var archivedContests = []
                for(var i in result) {
                (function(i) {
                    var item = result[i];
                    var accepted = item.contestant && item.contestant.accepted;
                    (accepted ? (item.contest.archived ? archivedContests : mainContests) : otherContests)
                        .push({
                            name: item.contest.name,
                            description: item.contest.description,
                            open: function() {
                                location.hash = '#/contest/' + item.contest.id
                            }
                        })
                })(i);
                 }
                self.contests([
                    {name: 'My contests', contests: mainContests},
                    {name: 'My archived contests', contests: archivedContests},
                    {name: 'Other contests', contests: otherContests},
                ]);
            });
        });

        this.get('/', function () {
            location.hash = '#/contests'
        });

        this._checkFormSubmission = function(form) {
            return false;
        };
    })
    app.raise_error = true;
    app.run();
}

var RARE = 'rare';
var REFRESH = 'refresh';
var CACHE = {};
var epochId = 1;

function getCached(type, url, callback, startEpoch) {
    if(!startEpoch)
        startEpoch = epochId;
    if(startEpoch != epochId)
        return;

    if(type != REFRESH) {
        if(typeof CACHE[url] !== 'undefined') {
            callback(JSON.parse(CACHE[url]));
            if(type == RARE) return;
        }
    }
    $.get(url, function(data) {
        if(startEpoch != epochId) return;
        var dataString = JSON.stringify(data);
        if(!CACHE[url] || CACHE[url] != dataString) {
            CACHE[url] = dataString;
            callback(data);
        }
    });
}

var vm
ko.applyBindings(vm = new SatoriViewModel());
