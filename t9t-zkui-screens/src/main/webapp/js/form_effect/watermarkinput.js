/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE. 
 */
 
/*
 * Version: Beta 1
 * Release: 2007-06-01
 */ 
(function($) {
	
	var isExist = false;
	var selectedObj = null;
	
	$.fn.Watermark = function(text,color) {
		return $.fn.Watermark(text,null,color);
	};
	
	$.fn.Watermark = function(text,selected,color) {
		if(!color)
			color="#aaa";
		return this.each(
			function(){		
				var input=$(this);
				var defaultColor='black';				
				function clearMessage(){
					if(input.val()==text) {
						input.val("");						
					} 
					
					if(selectedObj != null) {
						if(input.val()==text+' '+selectedObj) {
							input.val(selectedObj);
						}
					}
					
					input.css("color",defaultColor);
				}

				function insertMessage(){
					input.val(input.val().replace(/^\s+|\s+$/g,''));
					if(input.val().length==0 || input.val()==text){
						input.val(text);
						input.css("color",color);	
					} else {
						if(input.val().length>0 && input.val()==selectedObj) {
							input.val(text+' '+selectedObj);
							input.css("color",color);
						} else if(input.val().length>0 && input.val()==text+' '+selectedObj) {
							input.css("color",color);							
						}
						else {
							input.css("color",defaultColor);							
						}
					}
				}
				
				function initMessage() {
					input.val(input.val().replace(/^\s+|\s+$/g,''));
					if(input.val().length==0 || input.val()==text){
						input.val(text);
						input.css("color",color);	
					}
					
					if(input.val().length>0 && input.val()==selected) {						
						if(!isExist) {
							isExist = true;
							selectedObj = selected;	
						}
						
						input.val(text+' '+selectedObj);
						input.css("color",color);
					}
					
					if (input.is(':focus')) {
						input.blur();
						input.focus();
					}					
					
				}

				input.focus(clearMessage);
				input.blur(insertMessage);								
				//input.change(insertMessage);
				
				initMessage();
								
			}
		);

	};
})(jQuery);