/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.utils

import java.io.File
import java.net.JarURLConnection
import java.net.URI
import java.net.URL
import java.net.URLConnection
import java.util.zip.ZipFile

/**
 * Provide resources helpers
 */
object Resources {
  def isDirectory(classLoader: ClassLoader, url: URL) = url.getProtocol match {
    case "file"                      => new File(url.toURI).isDirectory
    case "jar"                       => isZipResourceDirectory(url)
    case "zip"                       => isZipResourceDirectory(url)
    case "bundle" | "bundleresource" => isBundleResourceDirectory(classLoader, url)
    case "resource"                  => isGraalVMResourceDirectory(classLoader, url)
    case _                           =>
      throw new IllegalArgumentException(s"Cannot check isDirectory for a URL with protocol='${url.getProtocol}'")
  }

  @deprecated("Use isUrlConnectionADirectory(classLoader,urlConnection) instead.", "2.9")
  def isUrlConnectionADirectory(urlConnection: URLConnection): Boolean = {
    isUrlConnectionADirectory(null, urlConnection)
  }

  /**
   * Tries to work out whether the given URL connection is a directory or not.
   *
   * Depends on the URL connection type whether it's accurate.  If it's unable to determine whether it's a directory,
   * this returns false.
   */
  def isUrlConnectionADirectory(classLoader: ClassLoader, urlConnection: URLConnection): Boolean = {
    (urlConnection, urlConnection.getURL.getProtocol) match {
      case (file, "file") =>
        new File(file.getURL.toURI).isDirectory
      case (jar: JarURLConnection, _) =>
        isJarURLConnectionDirectory(jar)
      case (bundle, "bundle") =>
        isBundleResourceDirectory(classLoader, bundle)
      case (bundleResource, "bundleresource") =>
        isBundleResourceDirectory(classLoader, bundleResource)
      case (resource, "resource") =>
        isGraalVMResourceDirectory(classLoader, resource)
      case _ =>
        false
    }
  }

  /**
   * Close a URL connection.
   *
   * This works around a JDK bug where if the URL connection is to a JAR file, and the entry is a directory, an NPE is
   * thrown.
   */
  def closeUrlConnection(connection: URLConnection): Unit = {
    connection match {
      case jar: JarURLConnection =>
        if (!jar.getUseCaches) {
          jar.getJarFile.close()
        }
      case other =>
        other.getInputStream.close()
    }
  }

  private def isZipResourceDirectory(url: URL): Boolean = {
    val path      = url.getPath
    val bangIndex = url.getFile.indexOf("!")

    val startIndex      = if (path.startsWith("zip:")) 4 else 0
    val fileUri         = path.substring(startIndex, bangIndex)
    val fileProtocol    = if (fileUri.startsWith("/")) "file://" else ""
    val absoluteFileUri = fileProtocol + fileUri

    val zipFile: File = new File(URI.create(absoluteFileUri))
    val resourcePath  = URI.create(path.substring(bangIndex + 1)).getPath.drop(1)
    val zip           = new ZipFile(zipFile)

    try {
      val entry = zip.getEntry(resourcePath)
      if (entry.isDirectory) true
      else {
        val stream = zip.getInputStream(entry)
        val isDir  = stream == null
        if (stream != null) stream.close()
        isDir
      }
    } finally {
      zip.close()
    }
  }

  private def isJarURLConnectionDirectory(jar: JarURLConnection): Boolean = {
    if (jar.getJarEntry.isDirectory) {
      true
    } else {
      // JarEntry.isDirectory is rubbish....
      val is = jar.getJarFile.getInputStream(jar.getJarEntry)
      if (is == null) {
        true
      } else {
        is.close()
        false
      }
    }
  }

  private def isBundleResourceDirectory(classLoader: ClassLoader, urlConnection: URLConnection): Boolean = {
    isBundleResourceDirectory(classLoader, urlConnection.getURL)
  }

  private def isBundleResourceDirectory(classLoader: ClassLoader, url: URL): Boolean = {
    if (classLoader != null) {
      /* ClassLoader within an OSGi container behave differently than the standard classloader.
       * One difference is how getResource returns when the resource's name end with a slash.
       * In a standard JVM, getResource doesn't care of ending slashes, and return the URL of
       * any existing resources. In an OSGi container (tested with Apache Felix), ending slashes
       * refers to a directory (return null otherwise). */

      val path      = url.getPath
      val pathSlash = if (path.last == '/') path else path + '/'

      classLoader.getResource(path) != null && classLoader.getResource(pathSlash) != null
    } else {
      false
    }
  }

  private def isGraalVMResourceDirectory(classLoader: ClassLoader, urlConnection: URLConnection): Boolean = {
    isGraalVMResourceDirectory(classLoader, urlConnection.getURL)
  }

  private def isGraalVMResourceDirectory(classLoader: ClassLoader, url: URL): Boolean = {
    /* Resource access in GraalVM can be considered the same as OSGi bundle resources. */
    isBundleResourceDirectory(classLoader, url)
  }
}
