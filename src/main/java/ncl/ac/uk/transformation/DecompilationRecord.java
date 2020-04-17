package ncl.ac.uk.transformation;

/**
 * A pair of translated language samples,
 * one in the low level (machine code) language
 * and one in the high level (source code) language.
 *
 * @param <LOW>  The sample representation type for the low level language.
 * @param <HIGH> The sample representation type for the high level language.
 */
public interface DecompilationRecord<LOW, HIGH> {

    LOW getLowLevelRepresentation();

    HIGH getHighLevelRepresentation();

    DecompilationRecord getPredecessor();
}
