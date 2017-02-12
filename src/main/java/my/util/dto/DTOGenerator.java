package my.util.dto;

/**
 * This class used to generate an object.
 * @author hubert
 *
 * @param <T>
 */
public interface DTOGenerator<T> {
	T generate(GeneratorContext pContext);
}
