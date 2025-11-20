package br.com.incidentemanager.helpdesk.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "layouts_email")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LayoutEmailEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "source_email")
	private String sourceEmail;

	@Column(name = "subject")
	private String subject;

	@Lob
	@Column(name = "body", columnDefinition = "LONGTEXT")
	private String body;

}
