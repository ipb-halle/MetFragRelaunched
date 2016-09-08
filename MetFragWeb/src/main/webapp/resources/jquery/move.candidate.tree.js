$( document ).ready(function() {

	var box = $('#mainForm').children('div').eq(0);
	
	alert(box.attr('id'));
	
	box.offset({
	    left: 100,
	    top: 75
	});
	
	var drag = {
	    elem: null,
	    x: 0,
	    y: 0,
	    state: false
	};
	var delta = {
	    x: 0,
	    y: 0
	};
	
	box.mousedown(function(e) {
	    if (!drag.state) {
	        drag.elem = this;
	        this.style.backgroundColor = '#f00';
	        drag.x = e.pageX;
	        drag.y = e.pageY;
	        drag.state = true;
	    }
	    return false;
	});
	
	
	$(document).mousemove(function(e) {
	    if (drag.state) {
	        drag.elem.style.backgroundColor = '#f0f';
	
	        delta.x = e.pageX - drag.x;
	        delta.y = e.pageY - drag.y;
	
	        var cur_offset = $(drag.elem).offset();
	
	        $(drag.elem).offset({
	            left: (cur_offset.left + delta.x),
	            top: (cur_offset.top + delta.y)
	        });
	
	        drag.x = e.pageX;
	        drag.y = e.pageY;
	    }
	});
	
	$(document).mouseup(function() {
	    if (drag.state) {
	        drag.elem.style.backgroundColor = '#808';
	    drag.state = false;
	}
	});
});