package staryhroft.templog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import staryhroft.templog.entity.enums.FavoriteStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "city_temperatures")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CityTemperature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = false)
    private Double temperature;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
