/*
 * Copyright 2013 eXo Platform SAS
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

package juzu.impl.common;

import java.io.IOException;

/**
 * An uri writer.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class URIWriter {

  /** . */
  private MimeType mimeType;

  /** . */
  private Appendable appendable;

  /** . */
  private boolean questionMarkDone;

  /**
   * Create a new URI writer.
   *
   * @param appendable the appendable
   * @param mimeType   the mime type
   * @throws NullPointerException if the appendable argument is null
   */
  public URIWriter(Appendable appendable, MimeType mimeType) throws NullPointerException {
    if (appendable == null) {
      throw new NullPointerException("No null appendable accepted");
    }

    //
    this.appendable = appendable;
    this.mimeType = mimeType;
  }

  /**
   * Create a new URI writer.
   *
   * @param appendable the appendable
   * @throws NullPointerException if the appendable argument is null
   */
  public URIWriter(Appendable appendable) throws NullPointerException {
    this(appendable, null);
  }

  public MimeType getMimeType() {
    return mimeType;
  }

  public void setMimeType(MimeType mimeType) {
    this.mimeType = mimeType;
  }

  public void append(char c) throws IOException {
    appendable.append(c);
  }

  public void append(String s) throws IOException {
    appendable.append(s);
  }

  /**
   * Append a segment to the path.
   *
   * @param c the char to append
   * @throws IllegalStateException if a query parameter was already appended
   * @throws IOException           any IO exception
   */
  public void appendSegment(char c) throws IllegalStateException, IOException {
    if (questionMarkDone) {
      throw new IllegalStateException("Query separator already written");
    }
    PercentCodec.PATH_SEGMENT.encode(c, appendable);
  }

  /**
   * Append a segment to the path.
   *
   * @param s the string to append.
   * @throws NullPointerException  if any argument value is null
   * @throws IllegalStateException if a query parameter was already appended
   * @throws IOException           any IO exception
   */
  public void appendSegment(String s) throws NullPointerException, IllegalStateException, IOException {
    if (s == null) {
      throw new NullPointerException("No null path accepted");
    }
    for (int len = s.length(), i = 0;i < len;i++) {
      char c = s.charAt(i);
      appendSegment(c);
    }
  }

  /**
   * Append a query parameter to the parameter set. Note that the query parameters are ordered and the sequence of call
   * to this method should be honoured when an URL is generated. Note also that the same parameter name can be used
   * multiple times.
   *
   * @param parameterName  the parameter name
   * @param paramaterValue the parameter value
   * @throws NullPointerException if any argument value is null
   * @throws IOException          any IOException
   */
  public void appendQueryParameter(String parameterName, String paramaterValue) throws NullPointerException, IOException {
    if (parameterName == null) {
      throw new NullPointerException("No null parameter name accepted");
    }
    if (paramaterValue == null) {
      throw new NullPointerException("No null parameter value accepted");
    }

    //
    MimeType mt = mimeType;
    if (mt == null) {
      mt = MimeType.XHTML;
    }

    //
    appendable.append(questionMarkDone ? mt.amp : "?");
    PercentCodec.QUERY_PARAM.encode(parameterName, appendable);
    appendable.append('=');
    PercentCodec.QUERY_PARAM.encode(paramaterValue, appendable);
    questionMarkDone = true;
  }

  /**
   * Reset the writer for reuse.
   *
   * @param appendable the used appendable
   */
  public void reset(Appendable appendable) {
    this.appendable = appendable;
    this.questionMarkDone = false;
  }
}