(function($){	
	
	// -------------------------------------------------------------------------------------------------------
	// Form Validation script - used by the Contact Form script
	// -------------------------------------------------------------------------------------------------------
	
	function validateMyAjaxInputs() {

		$.validity.start();
		// Validator methods go here:
		$("#name").require();
		$("#email").require().match("email");
		$("#subject").require();	

		// End the validation session:
		var result = $.validity.end();
		return result.valid;
	}
	
	// -------------------------------------------------------------------------------------------------------
	// ClearForm 
	// -------------------------------------------------------------------------------------------------------
	
	$.fn.clearForm = function() {
		return this.each(function() {
		var type = this.type, tag = this.tagName.toLowerCase();
		if (tag == 'form')
		return $(':input',this).clearForm();
		if (type == 'text' || type == 'password' || tag == 'textarea')
		this.value = '';
		else if (type == 'checkbox' || type == 'radio')
		this.checked = false;
		else if (tag == 'select')
		this.selectedIndex = -1;
		});
	};

	$(document).ready(function(){
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////						   
		
		// -------------------------------------------------------------------------------------------------------
		// Dropdown Menu
		// -------------------------------------------------------------------------------------------------------
		
		$("ul#dropdown-menu li").hover(function () {							 
			$(this).addClass("hover");
			$('ul:first', this).css({visibility: "visible",display: "none"}).slideDown(200);
		}, function () {
			$(this).removeClass("hover");
			$('ul:first', this).css({visibility: "hidden"});
		});
		
		if ( ! ( $.browser.msie && ($.browser.version == 6) ) ){
			$("ul#dropdown-menu li ul li:has(ul)").find("a:first").addClass("arrow");
		}
								
		// -------------------------------------------------------------------------------------------------------
		// Contact Form 
		// -------------------------------------------------------------------------------------------------------
		
		$("#contact-form").submit(function () {
											
			if (validateMyAjaxInputs()) { //  procced only if form has been validated ok with validity
				var str = $(this).serialize();
				$.ajax({
					type: "POST",
					url: "_layout/php/send.php",
					data: str,
					success: function (msg) {
						$("#formstatus").ajaxComplete(function (event, request, settings) {
							if (msg == 'OK') { // Message Sent? Show the 'Thank You' message
								result = '<div class="successmsg">Your message has been sent. Thank you!</div>';
								$('#contact-form').clearForm();
							} else {
								result = msg;
							}
							$(this).html(result);
						});
					}
		
				});
				return false;
			}
		});

		// -------------------------------------------------------------------------------------------------------
		//  Make entire service overviews clickable
		// -------------------------------------------------------------------------------------------------------
				 
		 $(".service-overview li").click(function(){
			 window.location=$(this).find("a").attr("href");
			 return false;
		});
		 
		// -------------------------------------------------------------------------------------------------------
		//  Make entire portfolio itemoverviews clickable
		// -------------------------------------------------------------------------------------------------------
				 
		 $(".portfolio-overview li").click(function () {
			window.location = $(this).find("a").attr("href");
			return false;
		});
		
		// -------------------------------------------------------------------------------------------------------
		//  Portfolio Filter - the filter dropdown
		// -------------------------------------------------------------------------------------------------------
		
		$("ul#portfolio-filter li").hover(function () {
			$(this).addClass("hover");
			$('ul:first', this).css({
				visibility: "visible",
				display: "none"
			}).slideDown(200);
		}, function () {
			$(this).removeClass("hover");
			$('ul:first', this).css({
				visibility: "hidden"
			});
		});
		
		// -------------------------------------------------------------------------------------------------------
		//  Protfolio Fade - on hover over a portfolio item overview the iamge gets faded
		// -------------------------------------------------------------------------------------------------------
		
		if ($.browser.msie && $.browser.version < 7) return;

		$(".portfolio-overview li img").fadeTo(1, 1);
		$(".portfolio-overview li").hover(
		
		function () {
			$(this).find('img').fadeTo("fast", 0.80);
		}, function () {
			$(this).find('img').fadeTo("slow", 1);
		});
	

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	});
	
})(window.jQuery);	

// non jQuery scripts below