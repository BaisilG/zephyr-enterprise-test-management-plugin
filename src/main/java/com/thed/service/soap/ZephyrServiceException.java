
package com.thed.service.soap;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "exception", targetNamespace = "http://soap.service.thed.com/")
public class ZephyrServiceException
    extends java.lang.Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private com.thed.service.soap.Exception faultInfo;

    /**
     * 
     * @param faultInfo
     * @param message
     */
    public ZephyrServiceException(String message, com.thed.service.soap.Exception faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param faultInfo
     * @param cause
     * @param message
     */
    public ZephyrServiceException(String message, com.thed.service.soap.Exception faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: com.thed.service.soap.Exception
     */
    public com.thed.service.soap.Exception getFaultInfo() {
        return faultInfo;
    }

}
