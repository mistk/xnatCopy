package my.util.dto;

import my.util.KeyConstants;

/**
 * is stand for a key in GenratorContext. this is framework's DTOConstants,
 * don't add any member about service layer, please extends this class.
 * different module use different DTOConstants.
 * 
 * @author hubert
 * @param <T>
 */
public class DTOConstants<T> extends KeyConstants<T> {
	public DTOConstants(String pName) {
		super(pName);
	}
	
	public static final KeyConstants<BaseDTO> BASE_DTO = new KeyConstants<>("baseDTO");
}
