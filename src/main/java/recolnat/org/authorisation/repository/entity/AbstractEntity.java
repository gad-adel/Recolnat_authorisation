package recolnat.org.authorisation.repository.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Abstract base class for entities. Allows parameterization of id type and implements {@link
 * #equals(Object)} and {@link #hashCode()} based on that id.
 *
 * @param <T> the type of the identifier.
 */
@ToString
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class AbstractEntity<T extends Serializable> {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(
          name = "UUID",
          strategy = "org.hibernate.id.UUIDGenerator"
  )
  @EqualsAndHashCode.Include
  private T id;


  /*
   * (non-Javadoc)
   * @see org.springframework.data.domain.Persistable#getId()
   */
  public T getId() {
    return id;
  }

  /**
   * Sets the id of the entity.
   *
   * @param id the id to set
   */
  public void setId(final T id) {
    this.id = id;
  }
}
