(function($){
		  
	$(document).ready(function(){
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////						   
		
		themeSliderTimeout = 5000;
		themeSliderPauseOnHover = true;
		
		// -------------------------------------------------------------------------------------------------------
		// Tipsy - facebook like tooltips jQuery plugin
		// -------------------------------------------------------------------------------------------------------
		
		$('.tip').tipsy({gravity: 'w', fade: true});
		
		// -------------------------------------------------------------------------------------------------------
		// pretyPhoto - jQuery lightbox plugin
		// -------------------------------------------------------------------------------------------------------
		
		$("a[rel^='prettyPhoto']").prettyPhoto({
			opacity: 0.80, 						// Value between 0 and 1
			show_title: false,
			default_width: 500,
			default_height: 500,
			theme: 'light_square', 				// light_rounded / dark_rounded / light_square / dark_square / facebook 
			hideflash: false, 					// Hides all the flash object on a page, set to TRUE if flash appears over prettyPhoto 
			modal: false						// If set to true, only the close button will close the window 
		});
		
		// -------------------------------------------------------------------------------------------------------
		// Cycle - slider jQuery plugin 
		// -------------------------------------------------------------------------------------------------------
		
		if ($('#slideshow-index').size()){
			
			$('#slideshow-index ul').cycle({
				timeout: themeSliderTimeout,	// milliseconds between slide transitions (0 to disable auto advance)
				fx: 'fade',						// choose your transition type, ex: fade, scrollUp, shuffle, etc...            
				prev: '#text-slideshow-prev',// selector for element to use as click trigger for next slide  
				next: '#text-slideshow-next',// selector for element to use as click trigger for previous slide
				pager: '#index-slideshow-pager',// selector for element to use as pager container
				delay: 0, 						// additional delay (in ms) for first transition (hint: can be negative)
				speed: 1000,  					// speed of the transition (any valid fx speed value) 
				pause: themeSliderPauseOnHover,	// true to enable "pause on hover"
				cleartypeNoBg: true,			// set to true to disable extra cleartype fixing (leave false to force background color setting on slides)
				pauseOnPagerHover: 0 			// true to pause when hovering over pager link
			});	
			
		}
		
		if ($('#slideshow-clients').size()){
			
			$('#slideshow-clients ul').cycle({
				timeout: 8000,// milliseconds between slide transitions (0 to disable auto advance)
				fx: 'fade',// choose your transition type, ex: fade, scrollUp, shuffle, etc...            
				delay: 0, // additional delay (in ms) for first transition (hint: can be negative)
				speed: 1000,  // speed of the transition (any valid fx speed value) 
				pause: true,// true to enable "pause on hover"
				cleartypeNoBg: true,// set to true to disable extra cleartype fixing (leave false to force background color setting on slides)
				pauseOnPagerHover: 0 // true to pause when hovering over pager link
			});

		}
		
		if ($('#slideshow-portfolio').size()){
			
			$('#slideshow-portfolio ul').cycle({
				timeout: themeSliderTimeout,	     // milliseconds between slide transitions (0 to disable auto advance)
				fx: 'fade',						     // choose your transition type, ex: fade, scrollUp, shuffle, etc...            
				pager: '#slideshow-portfolio-pager', // selector for element to use as pager container
				delay: 0, 						     // additional delay (in ms) for first transition (hint: can be negative)
				speed: 1000,  					     // speed of the transition (any valid fx speed value) 
				pause: themeSliderPauseOnHover,	    // true to enable "pause on hover"
				cleartypeNoBg: true,			    // set to true to disable extra cleartype fixing (leave false to force background color setting on slides)
				pauseOnPagerHover: 0 			    // true to pause when hovering over pager link
			});
		
		}
		
		// -------------------------------------------------------------------------------------------------------
		//  Tabify - jQuery tabs plugin
		// -------------------------------------------------------------------------------------------------------
		
		$('#tab-1, #tab-2, #tab-3, #tab-4, #tab-5').tabify();
		
		// -------------------------------------------------------------------------------------------------------
		//  Accordeon - jQuery accordeon plugin
		// -------------------------------------------------------------------------------------------------------
		
		$('#accordion-1, #accordion-2, #accordion-3, #accordion-4, #accordion-5').accordion();

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	});

})(window.jQuery);

// non jQuery plugins below

