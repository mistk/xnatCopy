$(function () {
	$('#typesSelect').change(function(e) {
		$.get({
			url: '',
			dataType: 'html',
			data: {
				action: 'getSubtypes', typeName: $(this).val()
			},
			success: function(resp) {
				console.log('resp: ', resp);
			}
		})
//		console.log(e);
	});
	$('#beanChangePropertyForm').submit(function() {
        // inside event callbacks 'this' is the DOM element so we first 
        // wrap it in a jQuery object and then invoke ajaxSubmit 
		$(this).ajaxSubmit({
			type: 'post',
			// timeout:   3000,
	        // other available options: 
	        //url:       url         // override for form's 'action' attribute 
	        //type:      type        // 'get' or 'post', override for form's 'method' attribute 
	        //dataType:  null        // 'xml', 'script', or 'json' (expected server response type) 
	        //clearForm: true        // clear all form fields after successful submit 
	        //resetForm: true        // reset the form after successful submit
			// pre-submit callback
			beforeSubmit: function(formData, jqForm, options) {
			    // formData is an array; here we use $.param to convert it to a string to display it 
			    // but the form plugin does this for you automatically when it submits the data 
			    var formParams = $.param(formData); 
			 
			    // jqForm is a jQuery object encapsulating the form element.  To access the 
			    // DOM element for the form do this: 
			    // var formElement = jqForm[0]; 
			    console.log('form params: ', formParams);
			 
			    var valid = this.validate(jqForm);
			    // here we could return false to prevent the form from being submitted; 
			    // returning anything other than false will allow the form submit to continue 
			    return valid; 
			},
			validate: function(jqForm) {
			    // jqForm is a jQuery object which wraps the form DOM element 
			    // 
			    // To validate, we can access the DOM elements directly and return true 
			 
			    var form = jqForm[0];
			    if (!form.newValue.value) return false;
			    return true;
			},
			// post-submit callback
			success: function(responseText, statusText, xhr, $form) {
			    // for normal html responses, the first argument to the success callback 
			    // is the XMLHttpRequest object's responseText property 
			 
			    // if the ajaxForm method was passed an Options Object with the dataType 
			    // property set to 'xml' then the first argument to the success callback 
			    // is the XMLHttpRequest object's responseXML property 
			 
			    // if the ajaxForm method was passed an Options Object with the dataType 
			    // property set to 'json' then the first argument to the success callback 
			    // is the json data object returned by the server 
			 
				console.log('status: ' + statusText + '\n\nresponseText: \n' + responseText + 
			        '\n\nThe output div should have already been updated with the responseText.');
			}
		});
        // !!! Important !!! 
        // always return false to prevent standard browser submit and page navigation 
        return false;
	});
//	$('#beanChangePropertyForm').ajaxForm({});
});
var dyn = {
	init: function(beansInfo) {
//		$('#beanSearchForm').form('submit', {
//			onSubmit: function(opt) {
//				console.log(opt);
//				return true;
//			}
//		});
//		var beansInfoObj = dyn.toJsonObj(beansInfo);
//		console.log('parsed json object: ', beansInfoObj);
//		for (var beanName in beansInfoObj) {
//			var beanObj = beansInfoObj[beanName];
//			$('#header').html(beanName);
//			console.log('beanObj: ', beanObj);
//			var propertyList = beanObj['propertyList'];
//		}
	},
	toJsonObj: function(jsonStr) {
		return JSON.parse(jsonStr);
	},
	
}