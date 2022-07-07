package fr.ralala.hexviewer.ui.adapters.search;
/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Interface used with SearchableListArrayAdapter
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public interface ISearchFrom {
  /**
   * Test if we aren't from the hex view or the plain view.
   *
   * @return boolean
   */
  boolean isSearchNotFromHewView();
}
