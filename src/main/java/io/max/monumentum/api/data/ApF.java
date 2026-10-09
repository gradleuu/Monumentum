package io.max.monumentum.api.data;

public enum ApF {
  SHARDS("shards.json");

  private final String fileName;

  ApF(String fileName) {
    this.fileName = fileName;
  }

  /**
   * @return the file name on the server, like {@code "shards.json"}
   */
  public String fileName() {
    return fileName;
  }
}
