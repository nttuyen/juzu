/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.application;

import juzu.UndeclaredIOException;
import juzu.impl.application.metadata.ApplicationDescriptor;
import juzu.impl.controller.descriptor.ControllerMethod;
import juzu.impl.controller.descriptor.ControllerMethodResolver;
import juzu.impl.inject.Export;
import juzu.impl.inject.ScopeController;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.inject.spi.InjectManager;
import juzu.impl.request.spi.ActionBridge;
import juzu.impl.request.spi.RenderBridge;
import juzu.impl.request.spi.RequestBridge;
import juzu.impl.request.spi.ResourceBridge;
import juzu.request.Phase;
import juzu.request.RequestContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Export
@Singleton
public class ApplicationContext {

  /** . */
  private final ApplicationDescriptor descriptor;

  /** . */
  final InjectManager<?, ?> injectManager;

  /** . */
  private final ControllerMethodResolver controllerResolver;

  /** . */
  public ArrayList<RequestFilter> lifecycles;

  @Inject
  public ApplicationContext(InjectManager injectManager, ApplicationDescriptor descriptor) throws Exception {
    this.descriptor = descriptor;
    this.injectManager = injectManager;
    this.controllerResolver = new ControllerMethodResolver(descriptor.getController());
  }

  // This is done lazyly to avoid circular references issues
  private <B, I> ArrayList<RequestFilter> getLifecycles(InjectManager<B, I> manager) throws Exception {
    if (lifecycles == null) {
      ArrayList<RequestFilter> lifeCycles = new ArrayList<RequestFilter>();
      for (B lifeCycleBean : manager.resolveBeans(RequestFilter.class)) {
        I lifeCycleInstance = manager.create(lifeCycleBean);
        RequestFilter lifeCycle = (RequestFilter)manager.get(lifeCycleBean, lifeCycleInstance);
        lifeCycles.add(lifeCycle);
      }
      lifecycles = lifeCycles;
    }
    return lifecycles;
  }

  public String getName() {
    return descriptor.getName();
  }

  public List<RequestFilter> getLifecycles() {
    try {
      return getLifecycles(injectManager);
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("handle me cracefully", e);
    }
  }

  public ClassLoader getClassLoader() {
    return injectManager.getClassLoader();
  }

  public ApplicationDescriptor getDescriptor() {
    return descriptor;
  }

  public InjectManager getInjectManager() {
    return injectManager;
  }

  public void invoke(RequestBridge bridge) throws ApplicationException {
    Phase phase;
    if (bridge instanceof RenderBridge) {
      phase = Phase.RENDER;
    }
    else if (bridge instanceof ActionBridge) {
      phase = Phase.ACTION;
    }
    else if (bridge instanceof ResourceBridge) {
      phase = Phase.RESOURCE;
    }
    else {
      throw new AssertionError();
    }

    //
    String methodId = bridge.getProperty(RequestContext.METHOD_ID);

    //
    Map<String, String[]> parameters = new HashMap<String, String[]>();
    for (Map.Entry<String, String[]> entry : bridge.getParameters().entrySet()) {
      String name = entry.getKey();
      String[] value = entry.getValue();
      if (name.startsWith("juzu.")) {
        if (name.equals("juzu.op")) {
          methodId = value[0];
        }
      }
      else {
        parameters.put(name, value);
      }
    }

    //
    ControllerMethod method;
    if (methodId == null) {
      method = controllerResolver.resolve(parameters.keySet());
    }
    else {
      method = controllerResolver.resolve(phase, methodId, parameters.keySet());
    }

    //
    if (method == null) {
      StringBuilder sb = new StringBuilder("handle me gracefully : no method could be resolved for " +
        "phase=" + phase + " and parameters={");
      int index = 0;
      for (Map.Entry<String, String[]> entry : bridge.getParameters().entrySet()) {
        if (index++ > 0) {
          sb.append(',');
        }
        sb.append(entry.getKey()).append('=').append(Arrays.asList(entry.getValue()));
      }
      sb.append("}");
      throw new UnsupportedOperationException(sb.toString());
    }

    //
    Object[] args = method.getArgs(parameters);
    Request request = new Request(this, method, parameters, args, bridge);

    //
    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
    try {
      ClassLoader classLoader = injectManager.getClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      ScopeController.begin(request);
      bridge.begin(request);
      request.invoke();
      try {
        bridge.end(request.getResponse());
      }
      catch (IOException e) {
        throw new UndeclaredIOException(e);
      }
    }
    finally {
      ScopeController.end();
      Thread.currentThread().setContextClassLoader(oldCL);
    }
  }

  public Object resolveBean(String name) throws ApplicationException {
    return resolveBean(injectManager, name);
  }

  private <B, I> Object resolveBean(InjectManager<B, I> manager, String name) throws ApplicationException {
    B bean = manager.resolveBean(name);
    if (bean != null) {
      try {
        I cc = manager.create(bean);
        return manager.get(bean, cc);
      }
      catch (InvocationTargetException e) {
        throw new ApplicationException(e.getCause());
      }
    }
    else {
      return null;
    }
  }
}
