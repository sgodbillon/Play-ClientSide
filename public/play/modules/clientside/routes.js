(function() {
	function mpkg(o, n) {
		if(n[0])
			return mpkg(o[n[0]] = o[n[0]] || {}, n.splice(1));
		return o;
	}
	
	function toArray(iterable, from, to) {
		if(!iterable || typeof iterable.length !== "number" || isNaN(iterable.length))
			throw "the object to copy into an array does not seem to be iterable!";
		from = from || 0;
		to = to || iterable.length;
		var result = [];
		for(; from < to; from++)
			result.push(iterable[from]);
		return result;
	}
	
	function _reverse() {
		var result = this.pattern;
		if(this.params === undefined) {
			if(console && console.log)
				console.log("This route is not supported (yet). No params names could be found.", this);
			return false;
		}
		if(arguments.length != this.patternArgs.length) return false;
		var params;
		for(var p = 0; p < this.params.length; p++) {
			if(this.params[0] && this.params[0].length === arguments.length) {
				params = this.params[0];
				break;
			}
		}
		if(params === undefined) return false;
		
		for(var i = 0; i < arguments.length; i++) {
			var argument = arguments[i];
			var matchingParam;
			for(var j = 0; j < params.length; j++) {
				if(params[j] === argument) {
					matchingParam = params[j];
					break;
				}
			}
			result = result.replace("{" + params[i] + "}", argument);
		}
		return result;
	}
	
	function Router() { }
	
	Router.prototype = {
		reverse: function(action) {
			var args = toArray(arguments, 1)
			var result;
			this.routes.forEach(function(route) {
				if(result) return;
				if(route.action === action)
					result = route.reverse.apply(route, args);
			});
			return result;
		},
		init: function(data) {
			this.routes = data;
			this.routes.forEach(function(route) {
				var patternMatcher = /\{[^}]+\}/g;
				route.patternArgs = [];
				var patternArg;
				while(patternArg = patternMatcher.exec(route.pattern)) {
					route.patternArgs.push(patternArg[0].substring(1, patternArg[0].length - 1));
				}
				route.reverse = _reverse;
			});
			return this;
		}
	};
	
	Router.load = function(url, callback) {
		var req = new XMLHttpRequest();
		req.open('GET', url, true);
		req.onreadystatechange = function() {
			if (req.readyState == 4) {
				if (req.status == 200) {
					var router = new Router().init(JSON.parse(req.responseText));
					if(typeof callback === "function")
						callback(router);
				}
				else
					throw new "Cannot load url: " + url;
			}
		};
		req.send(null);
	};
	
	mpkg(window, "play.modules.clientside".split(".")).Router = Router;
})();