package juzu.impl.inject.spi.declared.bean.scope.declared;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bean {

  /** . */
  private String value;

  public Bean() {
    this.value = "" + Math.random();
  }

  public String getValue() {
    return value;
  }
}