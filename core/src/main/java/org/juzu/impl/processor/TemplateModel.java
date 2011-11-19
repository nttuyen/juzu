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

package org.juzu.impl.processor;

import org.juzu.impl.template.ASTNode;
import org.juzu.impl.utils.FQN;

import java.io.Serializable;
import java.util.LinkedHashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateModel implements Serializable
{

   /** . */
   private final Foo foo;

   /** . */
   private final ASTNode.Template ast;

   /** . */
   private final FQN fqn;

   /** . */
   private final LinkedHashSet<String> parameters;

   public TemplateModel(
      Foo foo,
      ASTNode.Template ast,
      FQN fqn,
      LinkedHashSet<String> parameters)
   {
      this.foo = foo;
      this.ast = ast;
      this.fqn = fqn;
      this.parameters = parameters;
   }

   public Foo getFoo()
   {
      return foo;
   }

   public ASTNode.Template getAST()
   {
      return ast;
   }

   public FQN getFQN()
   {
      return fqn;
   }

   public LinkedHashSet<String> getParameters()
   {
      return parameters;
   }
}
