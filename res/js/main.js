
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
            self.contests([
                {name: 'Joined contests', contests: [
                    {name: 'Playground', description: 'Satori Playground'},
                    {name: 'MP, 2014/15', description: 'Metody programowania, semestr letni 2014/2015'}
                ]}
            ]);
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
