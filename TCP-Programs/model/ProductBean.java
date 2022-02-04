package model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "product")
public class ProductBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private String cost;


  public ProductBean() { }

  // Getters

  public String getId() { return id; }
  public String getName() { return name; }
  public String getCost() { return cost; }

  // Setters

  public void setId(String id) { this.id = id; }
  public void setName(String name) { this.name = name; }
  public void setCost(String cost) { this.cost = cost; }


  public String toString() {
    return String.format("Taxes in %s (%s):\n"
      + "- Id = %s\n"
      + "- Name  = %.2f%%\n"
      + "- cost  = %.2f%%\n", id, name, cost);
  }
}
