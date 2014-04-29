/***
 * Dany Maillard
 */
package sudoku;

public interface SudokuListener
{
	public void isComplet(Object o);
	public void erreur(Object o);
	public void historiqueChange(Object o);
}
