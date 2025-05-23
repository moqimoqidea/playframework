/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package play.core.cookie.encoding;

import static play.core.cookie.encoding.CookieUtil.*;

import java.util.*;

/**
 * A <a href="https://tools.ietf.org/html/rfc6265">RFC6265</a> compliant cookie encoder to be used
 * server side, so some fields are sent (Version is typically ignored).
 *
 * <p>As Netty's Cookie merges Expires and MaxAge into one single field, only Max-Age field is sent.
 *
 * <p>Note that multiple cookies are supposed to be sent at once in a single "Set-Cookie" header.
 *
 * @see ServerCookieDecoder
 */
public final class ServerCookieEncoder extends CookieEncoder {

  /**
   * Strict encoder that validates that name and value chars are in the valid scope defined in
   * RFC6265
   */
  public static final ServerCookieEncoder STRICT = new ServerCookieEncoder(true);

  /** Lax instance that doesn't validate name and value */
  public static final ServerCookieEncoder LAX = new ServerCookieEncoder(false);

  private ServerCookieEncoder(boolean strict) {
    super(strict);
  }

  /**
   * Encodes the specified cookie name-value pair into a Set-Cookie header value.
   *
   * @param name the cookie name
   * @param value the cookie value
   * @return a single Set-Cookie header value
   */
  public String encode(String name, String value) {
    return encode(new DefaultCookie(name, value));
  }

  /**
   * Encodes the specified cookie into a Set-Cookie header value.
   *
   * @param cookie the cookie
   * @return a single Set-Cookie header value
   */
  public String encode(Cookie cookie) {
    if (cookie == null) {
      throw new NullPointerException("cookie");
    }
    final String name = cookie.name();
    final String value = cookie.value() != null ? cookie.value() : "";

    validateCookie(name, value);

    StringBuilder buf = new StringBuilder();

    if (cookie.wrap()) {
      addQuoted(buf, name, value);
    } else {
      add(buf, name, value);
    }

    if (cookie.maxAge() != Integer.MIN_VALUE) {
      add(buf, CookieHeaderNames.MAX_AGE, cookie.maxAge());
      Date expires =
          cookie.maxAge() <= 0
              ? new Date(0) // Set expires to the Unix epoch
              : new Date(cookie.maxAge() * 1000L + System.currentTimeMillis());
      add(buf, CookieHeaderNames.EXPIRES, HttpHeaderDateFormat.get().format(expires));
    }

    if (cookie.sameSite() != null) {
      add(buf, CookieHeaderNames.SAMESITE, cookie.sameSite());
    }

    if (cookie.path() != null) {
      add(buf, CookieHeaderNames.PATH, cookie.path());
    }

    if (cookie.domain() != null) {
      add(buf, CookieHeaderNames.DOMAIN, cookie.domain());
    }
    if (cookie.isSecure()) {
      add(buf, CookieHeaderNames.SECURE);
    }
    if (cookie.isHttpOnly()) {
      add(buf, CookieHeaderNames.HTTPONLY);
    }
    if (cookie.isPartitioned()) {
      add(buf, CookieHeaderNames.PARTITIONED);
    }

    return stripTrailingSeparator(buf);
  }

  /**
   * Batch encodes cookies into Set-Cookie header values.
   *
   * @param cookies a bunch of cookies
   * @return the corresponding bunch of Set-Cookie headers
   */
  public List<String> encode(Cookie... cookies) {
    if (cookies == null) {
      throw new NullPointerException("cookies");
    }
    if (cookies.length == 0) {
      return Collections.emptyList();
    }

    List<String> encoded = new ArrayList<>(cookies.length);
    for (Cookie c : cookies) {
      if (c == null) {
        break;
      }
      encoded.add(encode(c));
    }
    return encoded;
  }

  /**
   * Batch encodes cookies into Set-Cookie header values.
   *
   * @param cookies a bunch of cookies
   * @return the corresponding bunch of Set-Cookie headers
   */
  public List<String> encode(Collection<? extends Cookie> cookies) {
    if (cookies == null) {
      throw new NullPointerException("cookies");
    }
    if (cookies.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> encoded = new ArrayList<>(cookies.size());
    for (Cookie c : cookies) {
      if (c == null) {
        break;
      }
      encoded.add(encode(c));
    }
    return encoded;
  }

  /**
   * Batch encodes cookies into Set-Cookie header values.
   *
   * @param cookies a bunch of cookies
   * @return the corresponding bunch of Set-Cookie headers
   */
  public List<String> encode(Iterable<? extends Cookie> cookies) {
    if (cookies == null) {
      throw new NullPointerException("cookies");
    }
    if (cookies.iterator().hasNext()) {
      return Collections.emptyList();
    }

    List<String> encoded = new ArrayList<>();
    for (Cookie c : cookies) {
      if (c == null) {
        break;
      }
      encoded.add(encode(c));
    }
    return encoded;
  }
}
