package com.github.karlnicholas.webfluxjwtsecurity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User class
 *
 * @author Karl Nicholas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("users")
public class User implements Persistable<String> {
    @Id
	private String username;
	private String password;
	private List<String> roles;
    private String firstName;
    private String lastName;
	private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Transient
    private boolean isNew;
	@Override
	public String getId() {
		return username;
	}
	@Override
	public boolean isNew() {
		return isNew;
	}
	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}
}
