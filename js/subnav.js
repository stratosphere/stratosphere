$('a[rel=tooltip]').tooltip({
	'placement': 'bottom'
});

// $('.subnav a').smoothScroll(); -- does not work with the offset scroll fix

(function ($) {

	$(function() {

		// fix sub nav on scroll
		var $win = $(window),
			$body = $('body'),
			$nav = $('.subnav'),
			navHeight = $('.navbar').first().height(),
			subnavHeight = $('.subnav').first().height(),
			subnavTop = $('.subnav').length && $('.subnav').offset().top - navHeight,
			marginTop = parseInt($body.css('margin-top'), 10);
			isFixed = 0;

		processScroll();

		$win.on('scroll', processScroll);

		function processScroll() {
			var i, scrollTop = $win.scrollTop();

			if (scrollTop >= subnavTop-1 && !isFixed) {
				isFixed = 1;
				$nav.addClass('subnav-fixed');
				$body.css('margin-top', marginTop + subnavHeight +  'px');
			} else if (scrollTop < subnavTop-1 && isFixed) {
				isFixed = 0;
				$nav.removeClass('subnav-fixed');
				$body.css('margin-top', marginTop + 'px');
			}
		}
		
		// Required if "viewport" content is "width=device-width, initial-scale=1.0": 
		// navbar is not fixed on lower widths.
		if ($(document).width() > 979) { 
			var hash = window.location.hash;

			// Code below fixes the issue if you land directly onto a page section
			// (http://domain/page.html#section1)

			if (hash != "") {
				$(document).scrollTop(($(hash).offset().top) - ($(".navbar-fixed-top").height() + $(".subnav").height() + 60));
			}

			// Here's the fix, if any <a> element points to a page section an offset
			// function is called

			var locationHref = window.location.protocol + '//' + window.location.host + $(window.location).attr('pathname');
			var anchorsList = $('a').get();

			for (i = 0; i < anchorsList.length; i++) {
				var hash = anchorsList[i].href.replace(locationHref, '');
				if (hash[0] == "#" && hash != "#") {
					// Retain your pre-defined onClick functions
					var originalOnClick = anchorsList[i].onclick; 
					setNewOnClick(originalOnClick, hash);
				}
			}
		}

		function setNewOnClick(originalOnClick, hash) {
			anchorsList[i].onclick = function() {
				$(originalOnClick);
				if (isFixed) {
					$(document).scrollTop( ($(hash).offset().top) - ($(".navbar-fixed-top").height() + $(".subnav").height()) - 20);
				} else {
					$(document).scrollTop( ($(hash).offset().top) - ($(".navbar-fixed-top").height() + $(".subnav").height()) - 62);
				}
				return false;
			};
		}
	});
	
	/*  
	 * Taken from: ivos@github (https://gist.github.com/ivos/4055810)
	 * 
	 * Overview:
	 * When you call a given section of a document (e.g. via <a href="#section"> or domain.com/page#section1) browsers show that section at the top of window. Bootstrap's navbar-fixed-top, _since it's fixed_, *overlays* first lines of content.
	 * This function catches all of the <a> tags pointing to a section of the page and rewrites them to properly display content.
	 * Works fine even if a # section is called from URL.
	 * 
	 * Usage:
	 * Paste this function wherever you want in your document and append _fixNavbarIssue()_ to your <body>'s onload attribute.
	 */
	
})(window.jQuery);