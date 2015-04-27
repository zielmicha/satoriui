
function SatoriViewModel() {
    var self = this;
    self.loginDialog = ko.observable(null);
    self.contests = ko.observable(null)

    self.clear = function() {
        self.loginDialog(undefined)
        self.contests(undefined)
    }

    var app = Sammy(function () {
        this.before({}, function() {
            self.clear()
        })

        this.get('#/login', function () {
            self.loginDialog({
                login: function() {
                    location.hash = '#/contests'
                }
            });
        });

        this.get('#/contests', function () {
            $.get('/contests', function(result) {
                console.log(result)
                var results = []
                for(var i in result) {
                    var item = result[i];
                    results.push({
                        name: item.contest.name,
                        description: item.contest.description
                    })
                }
                self.contests([
                    {name: 'All contests', contests: results}
                ]);
            });
        });

        this.get('/', function () {
            location.hash = '#/login'
        });

        this._checkFormSubmission = function(form) {
            return false;
        };
    })
    app.raise_error = true;
    app.run();
}

var vm
ko.applyBindings(vm = new SatoriViewModel());
