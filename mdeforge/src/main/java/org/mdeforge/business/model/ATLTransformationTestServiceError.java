package org.mdeforge.business.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class ATLTransformationTestServiceError implements Serializable{

	
	
	//INNER CLASS AND ENUMERATES
	
	
	
		
		/** attributes */
		private Model     model        = null;
		private String     errorMessage = "";
		public String getErrorMessage() {
			return errorMessage;
		}
		private ERROR_KIND errorKind    = null;
		
		public ERROR_KIND getErrorKind() {
			return errorKind;
		}
		public void setErrorKind(ERROR_KIND errorKind) {
			this.errorKind = errorKind;
		}
		//		public Record (Model model) { 
//			this.model = model; 
//		}
//		
//		public Record (Model model, String error, ERROR_KIND kind) { 
//			this(model);
//			this.errorMessage = error;
//			this.errorKind    = kind;
//		}
		public void setErrorMessage (String errorMessage) {this.errorMessage = errorMessage;}
		public Model  getModel()                  { return model; }
		public void   setModel(Model model) 	   {this.model = model;}
		@JsonIgnore
		public String  getError()                  { return (model!=null? "[witness model "+model+"]: " : "") + errorMessage; }
		@JsonIgnore
		public boolean getExecutionRaisesException () { return errorKind==ERROR_KIND.EXECUTION_RAISES_EXCEPTION;  }
		@JsonIgnore
		public boolean getExecutionYieldsIllTarget () { return errorKind==ERROR_KIND.EXECUTION_YIELDS_ILL_TARGET; }
		@JsonIgnore
		public boolean getAnatlyserNotifiesError ()   { return errorKind==ERROR_KIND.ANATLYZER_DETECTED_ERROR;    }
		@JsonIgnore
		public boolean getAnatlyserDoesNotFinish ()   { return errorKind==ERROR_KIND.ANATLYZER_DOES_NOT_FINISH;   }
		
		
	
}
