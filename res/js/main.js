
function SatoriViewModel() {
    var self = this;
    self.loginDialog = ko.observable(null);
    self.contests = ko.observable(null);
    self.contest = ko.observable(null);
    self.subpage = ko.observable(null);
    self.news = ko.observable(null);

    self.clear = function() {
        self.loginDialog(undefined);
        self.contests(undefined);
        self.contest(undefined);
        self.subpage(undefined);
        self.news(undefined);
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
            loadContest(this, function(result) {
                self.results([])
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
                var results = []
                for(var i in result) {
                (function(i) {
                    var item = result[i];
                    var accepted = !item.contestant || item.contestant.accepted;
                    if(accepted && !item.contest.archived)
                        results.push({
                            name: item.contest.name,
                            description: item.contest.description,
                            open: function() {
                                location.hash = '#/contest/' + item.contest.id
                            }
                        })
                })(i);
                 }
                self.contests([
                    {name: 'All contests', contests: results}
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

var RARE = 'rare'
var CACHE = {}

function getCached(type, url, callback) {
    if(typeof CACHE[url] !== 'undefined') {
        callback(JSON.parse(CACHE[url]));
        if(type == RARE) return;
    }
    $.get(url, function(data) {
        CACHE[url] = JSON.stringify(data);
        callback(data);
    });
}

var vm
ko.applyBindings(vm = new SatoriViewModel());
