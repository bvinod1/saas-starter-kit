/*******************************************************************************
 *
 * Copyright (c) 2020-21 Mindsprint Limited
 *
 * All information contained herein is, and remains the property of Mindsprint
 * Limited. The intellectual and technical concepts contained herein are
 * proprietary to Mindsprint and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from Mindsprint
 * Limited
 *
 *******************************************************************************/
package com.saasstarterkit.user.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Indicates a class that declares one or more @Bean methods and also triggers component scanning. 
 * This is a convenience annotation that is equivalent to declaring @ComponentScan and @Import.
 * 
 * @author Mohankumar.D
 * @version 1.2.34a
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CentralizedIAMConfig.class)
public @interface EnableCentralizedIAM {

}
