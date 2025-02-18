/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import scala.Option;
import scala.jdk.javaapi.OptionConverters;

/**
 * The environment for the application.
 *
 * <p>Captures concerns relating to the classloader and the filesystem for the application.
 */
// @Singleton, see BuiltInModule (We don't want jakarta.inject inside play-configuration project)
public class Environment {
  private final play.api.Environment env;

  public Environment(play.api.Environment environment) {
    this.env = environment;
  }

  public Environment(File rootPath, ClassLoader classLoader, Mode mode) {
    this(new play.api.Environment(rootPath, classLoader, mode.asScala()));
  }

  public Environment(File rootPath, Mode mode) {
    this(rootPath, Environment.class.getClassLoader(), mode);
  }

  public Environment(File rootPath) {
    this(rootPath, Environment.class.getClassLoader(), Mode.TEST);
  }

  public Environment(Mode mode) {
    this(new File("."), Environment.class.getClassLoader(), mode);
  }

  /**
   * The root path that the application is deployed at.
   *
   * @return the path
   */
  public File rootPath() {
    return env.rootPath();
  }

  /**
   * The classloader that all application classes and resources can be loaded from.
   *
   * @return the class loader
   */
  public ClassLoader classLoader() {
    return env.classLoader();
  }

  /**
   * The mode of the application.
   *
   * @return the mode
   */
  public Mode mode() {
    return env.mode().asJava();
  }

  /**
   * Returns `true` if the application is `DEV` mode.
   *
   * @return `true` if the application is `DEV` mode.
   */
  public boolean isDev() {
    return mode().equals(Mode.DEV);
  }

  /**
   * Returns `true` if the application is `PROD` mode.
   *
   * @return `true` if the application is `PROD` mode.
   */
  public boolean isProd() {
    return mode().equals(Mode.PROD);
  }

  /**
   * Returns `true` if the application is `TEST` mode.
   *
   * @return `true` if the application is `TEST` mode.
   */
  public boolean isTest() {
    return mode().equals(Mode.TEST);
  }

  /**
   * Retrieves a file relative to the application root path.
   *
   * @param relativePath relative path of the file to fetch
   * @return a file instance - it is not guaranteed that the file exists
   */
  public File getFile(String relativePath) {
    return env.getFile(relativePath);
  }

  /**
   * Retrieves a file relative to the application root path. This method returns an Optional, using
   * empty if the file was not found.
   *
   * @param relativePath relative path of the file to fetch
   * @return an existing file
   */
  public Optional<File> getExistingFile(String relativePath) {
    return OptionConverters.toJava(env.getExistingFile(relativePath));
  }

  /**
   * Retrieves a resource from the classpath.
   *
   * @param relativePath relative path of the resource to fetch
   * @return URL to the resource (may be null)
   */
  public URL resource(String relativePath) {
    final Option<URL> res = env.resource(relativePath);
    if (res.isDefined()) {
      return res.get();
    }
    return null;
  }

  /**
   * Retrieves a resource stream from the classpath.
   *
   * @param relativePath relative path of the resource to fetch
   * @return InputStream to the resource (may be null)
   */
  public InputStream resourceAsStream(String relativePath) {
    final Option<InputStream> res = env.resourceAsStream(relativePath);
    if (res.isDefined()) {
      return res.get();
    }
    return null;
  }

  /**
   * A simple environment.
   *
   * <p>Uses the same classloader that the environment classloader is defined in, the current
   * working directory as the path and test mode.
   *
   * @return the environment
   */
  public static Environment simple() {
    return new Environment(new File("."), Environment.class.getClassLoader(), Mode.TEST);
  }

  /**
   * The underlying Scala API Environment object that this Environment wraps.
   *
   * @return the environment
   * @see play.api.Environment
   */
  public play.api.Environment asScala() {
    return env;
  }
}
