/***
 * Dany Maillard
 */
package sudoku;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

public class Case extends JLabel implements Cloneable
{
	private ArrayList<CaseListener> listeners = new ArrayList<CaseListener>();
	private boolean[] valeurs = new boolean[9];
	private int[] casesTouchees = new int[9];
	private JLabel[] labels = new JLabel[9];
	private int valeur = 0;
	private int x = 0;
	private int y = 0;
	private Color couleurAssignee = Color.BLUE;
	private Color couleurPossible = Color.GREEN;
	private Color couleurImpossible = Color.RED;
	private Color couleurNormal = Color.WHITE;
	private Color couleurProprietaire = Color.ORANGE;
	private Color couleurSansRisque = Color.YELLOW;
	private boolean historique = false;
	
	/***
	 * La classe Case est une case de sudoku
	 * @param x Coordonn�e x de la case
	 * @param y Coordonn�e y de la case
	 */
	public Case(int x, int y)
	{
		this.x = x;
		this.y = y;
		initGUI();
		reset();
	}
	
	/***
	 * Initiliase le design de la case
	 */
	private void initGUI()
	{
		// Design de la case
		setBorder(BorderFactory.createLineBorder(Color.black));
		setFont(new Font("monospaced", Font.PLAIN, 50));
		setVerticalAlignment(CENTER);
		setHorizontalAlignment(CENTER);
		setOpaque(true);
		
		// Design des sous-label pour les possibilit�s
		setLayout(new GridLayout(3, 3));
		for (int i = 0; i < 9; i++)
		{
			JLabel l = new JLabel();
			final int possibilite = i + 1;
			l.addMouseListener(new MouseListener()
			{
				public void mouseReleased(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseClicked(MouseEvent e)
				{
					setValeur(possibilite);
				}
			});
			l.setText(String.valueOf(i + 1));
			l.setBorder(BorderFactory.createDashedBorder(Color.WHITE, Paint.OPAQUE, 2));
			l.setFont(new Font("monospaced", Font.PLAIN, 12));
			l.setVerticalAlignment(CENTER);
			l.setHorizontalAlignment(CENTER);
			l.setOpaque(true);
			labels[i] = l;
			add(l);
		}
	}
	/***
	 * Efface un �couteur de case
	 * @param listener Ecouteur de case
	 */
	public void removeListener(CaseListener listener)
	{
		listeners.remove(listener);
	}

	/***
	 * Ajoute un �couteur de case
	 * @param listener Ecouteur de case
	 */
	public void addListener(CaseListener listener)
	{
		listeners.add(listener);
	}
	
	/***
	 * R�initialise la case
	 */
	public void reset()
	{
		valeur = 0;
		for (int i = 0; i < 9; i++)
		{
			valeurs[i] = true;
			setBackground(couleurNormal);
			labels[i].setBackground(couleurPossible);
			labels[i].setToolTipText("");
			labels[i].setVisible(true);
			casesTouchees[i] = 82;
		}
	}
	
	/***
	 * R�cup�re la coordonn�e x de la case
	 * @return coordonn�e y de la case
	 */
	public int x()
	{
		return this.x;
	}
	
	/***
	 * R�cup�re la coordonn�e y de la case
	 * @return Coordonn�e y de la case
	 */
	public int y()
	{
		return this.y;
	}
	
	/***
	 * R�cup�re la valeur assign�e de la case. 0 si pas de valeur.
	 * @return Valeur assign�e de la case
	 */
	public int valeur()
	{
		return this.valeur;
	}
	
	/***
	 * Assigne une valeur � la case
	 * @param valeur Valeur de la case � assigner
	 */
	public void setValeur(int valeur)
	{
		if(!isValeurAssignee())
		{
			this.valeur = valeur;
			valeurAssigneeSignal();
		}
		
		refresh();
	}
	
	/***
	 * Emet le signal qu'une valeur est assign�e
	 */
	private void valeurAssigneeSignal()
	{
		for (CaseListener cl : listeners)
		{
			cl.valeurAssignee(this);
		}
	}
	
	/**
	 * Assigne le nombre de case touch�es si une certaine valeur est affect�e � la case
	 * @param valeur
	 * @param casesTouchees
	 */
	public void setCasesTouchees(int valeur, int nombreCasesTouchees)
	{
		casesTouchees[valeur - 1] = nombreCasesTouchees;
		labels[valeur - 1].setToolTipText(String.valueOf(nombreCasesTouchees));
	}
	
	/***
	 * Retourne le nombre de cases touch�es pour une certaine valeur de la case
	 * @param valeur Valeur � tester 
	 * @return Nombre de cases touch�es par la valeur
	 */
	public int casesTouchees(int valeur)
	{
		return casesTouchees[valeur - 1];
	}
	
	/***
	 * Retourne les possibilites qui touchent le moins de cases, si ces possibilit�s devaient �tre affect�es � la cases
	 * @return Possibilit� la moins influante
	 */
	private ArrayList<Integer> possibilitesLesMoinsInfluantes()
	{
		int casesTouchees = 0;
		int casesToucheesMin = 82;
		ArrayList<Integer> possibilites = new ArrayList<>();
		
		for (int i = 1; i <= 9; i++)
		{
			if(isPossible(i))
			{
				casesTouchees = casesTouchees(i);
				
				if(casesTouchees == casesToucheesMin)
				{
					possibilites.add(i);
				}
				
				if(casesTouchees < casesToucheesMin)
				{
					casesToucheesMin = casesTouchees;
					possibilites.clear();
					possibilites.add(i);
				}
			}
		}
		
		return possibilites;
	}
	
	/***
	 * Retourne si une valeur est possible
	 * @param valeur Valeur � tester
	 * @return true si possible, false si impossible
	 */
	public boolean isPossible(int valeur)
	{
		return valeurs[valeur - 1];
	}
	
	/***
	 * Supprime une des 9 possibilit�s de la case.
	 * @param valeur Possibilit� � supprimer
	 * @return Valeur d'assignement de la case. 0 si pas d'assignement.
	 */
	public void supprimerPossibilite(int valeur)
	{
		valeurs[valeur - 1] = false;
		labels[valeur - 1].setBackground(couleurImpossible);

		//valeurAssignee = checkAssignationValeur();
		//checkAssignationValeur();
		
		//return valeurAssignee;
	}
	
	/***
	 * Check si il ne reste plus que une possibilit�. Si oui, assigne la derni�re possibilit� � la case.
	 * @return Possibilit� assign�e.
	 */
	private int checkAssignationValeur()
	{
		int valeur = 0;
		
		if(nombrePossibilites() == 1)
		{
			valeur = getPremierePossibilite();
			setValeur(valeur);
		}
		else
		{
			valeur = 0;
		}
		
		return valeur;
	}
	
	/***
	 * Retourne le nombre de possibilit�s restante.
	 * @return nombre de possibilit�s restante de la case.
	 */
	public int nombrePossibilites()
	{
		int somme = 0;
		
		for (int i = 0; i < valeurs.length; i++)
		{
			somme += valeurs[i] ? 1 : 0;
		}
		
		return somme;
	}
	
	/***
	 * Retourne la premi�re possibilit� de la case
	 * @return la premi�re possibilit� de la case
	 */
	private int getPremierePossibilite()
	{		
		for (int i = 0; i < valeurs.length; i++)
		{
			if(valeurs[i])
				return i + 1;
		}
		return 0;
	}
	
	/***
	 * Retourne une des possibilit�s de la case
	 * @return possibilit� al�atoire de la case
	 */
	private int getPossbiliteAleatoire()
	{
		Random random = new Random();
		ArrayList<Integer> possiblites = new ArrayList<>();
		int index = 0;
		int possibilite = 0;
		
		for (int i = 0; i < valeurs.length; i++)
		{
			if(valeurs[i])
				possiblites.add(i + 1);
		}
		
		index = Math.abs(random.nextInt() % possiblites.size());
		possibilite = possiblites.get(index);
		
		return possibilite;
	}
	
	/***
	 * Retourne vrai si une valeur est assign�e � la case
	 * @return
	 */
	public Boolean isValeurAssignee()
	{
		return (valeur != 0);
	}
	
	/***
	 * Rafraichit l'affichage de la case
	 */
	private void refresh()
	{
		if(isValeurAssignee())
		{
			setText(String.valueOf(this.valeur));
			setBackground(couleurAssignee);
			possibilitesVisible(false);
		}
		else
		{
			setText("");
			setBackground(couleurNormal);
			possibilitesVisible(true);
		}
	}
	
	/***
	 * Rend visible ou non les possibilit�s
	 * @param visible si true: affiche; si false: masque les possibilit�s
	 */
	private void possibilitesVisible(boolean visible)
	{
		for (JLabel l : labels)
		{
			l.setVisible(visible);
		}
	}
	
	/***
	 * Colore les possibilit�s restantes avec la couleur prioritaire
	 * @param proprietaire si true: couleur propri�taire; si false couleur possible.
	 */
	public void setPrioritaire(boolean proprietaire)
	{
		for (int i = 0; i < 9; i++)
		{
			if(valeurs[i])
			{
				if(proprietaire)
				{
					labels[i].setBackground(couleurProprietaire);
				}
				else
					labels[i].setBackground(couleurPossible);
			}
		}
	}
	
	/***
	 * Colore la possibilit� la moins risqu�e
	 */
	public void colorierPossibilitesIdeales()
	{
		ArrayList<Integer> possibilite = possibilitesLesMoinsInfluantes();
		for (Integer p : possibilite)
		{
			labels[p - 1].setBackground(couleurSansRisque);
		}
	}
	
	/***
	 * Retourne une des possibilites qui a le moins d'impacte sur ces cases voisines
	 * @return possibilites la moins influente
	 */
	private int getPossibiliteIdeale()
	{
		int possibiliteIdeale = 0;
		Random random = new Random();
		ArrayList<Integer> possibilites = possibilitesLesMoinsInfluantes();
		int index = 0;
		
		index = Math.abs(random.nextInt() % possibilites.size());
		possibiliteIdeale = possibilites.get(index);
		
		return possibiliteIdeale;
	}
	
	/***
	 * Assigne la valeur id�ale � la case
	 */
	public void setValeurIdeale()
	{
		setValeur(getPossibiliteIdeale());
	}
	
	/***
	 * Assigne la premi�re valeur � la case
	 */
	public void setPremiereValeur()
	{
		setValeur(getPremierePossibilite());
	}
	/***
	 * Assigne une possibilit� possible et al�atoire � la case
	 */
	public void setValeurAleatoire()
	{
		setValeur(getPossbiliteAleatoire());
	}
	
	/***
	 * Retourne un clone de la Case
	 */
	protected Object clone()
	{
		Object o = null;
		
		try {
			o = super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		return o;
	}
	
	/***
	 * D�finit si la case est dans l'historique
	 */
	public void setHistorique(boolean historique)
	{
		this.historique = historique;
		
		for (int i = 0; i < 9; i++)
		{
			//labels[i].
		}
	}
	
	/***
	 * Retourne true si la case est dans l'historique
	 * @return true si la case est une case d'historique; false si la case est une case normal
	 */
	public boolean isHistorique()
	{
		return this.historique;
	}
}
