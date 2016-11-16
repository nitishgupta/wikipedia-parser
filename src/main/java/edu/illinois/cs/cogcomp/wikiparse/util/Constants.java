package edu.illinois.cs.cogcomp.wikiparse.util;

/**
 * Created by nitishgupta on 10/24/16.
 */
public class Constants {
	// mid \t alias/name - From freebase for all mids
	public static final String mid_alias_names_file = "/save/ngupta19/freebase/types_xiao/mid.alias.names";

	// mid \t fb_name \t wid - For all relevant types freebase entities
	public static final String mid_names_wid_filepath = "/save/ngupta19/freebase/types_xiao/mid.names.wiki_id_en";

	// wid - wids from freebase that are found in Wiki and not redirect/disambiguation
	public static final String wid_parsedInWiki_filepath = "/save/ngupta19/freebase/types_xiao/wid.parsedInWiki";

	// wid \t WikiTitle for final wids that are parsed in wiki, i.e. in wiki_kb_docsDir
	public static final String wid_WikiTitle_filepath = "/save/ngupta19/freebase/types_xiao/wid.WikiTitle";

	// redirectTitle \t WikiTitle for final wids
	public static final String redirectTitle_WikiTitle_filepath = "/save/ngupta19/freebase/types_xiao/redirect.wikiTitle";

	// Directory containing all docs_links text
	//public static final String wiki_kb_docsDir = "/save/ngupta19/wikipedia/wiki_kb_10_15/docs_links/";
	public static final String wiki_kb_docsDir = "/save/ngupta19/wikipedia/wiki_kb/docs/";
	public static final String wiki_kb_linksDir = "/save/ngupta19/wikipedia/wiki_kb/links/";

	// Wiki Docs and Links Vector
	public static final String wiki_doc_vectors_file = "/save/ngupta19/wikipedia/wiki_kb/doc.vectors";
	public static final String wiki_links_vectors_file = "/save/ngupta19/wikipedia/wiki_kb/links.vectors";

	// Filepath to file containing wikipedia mentions
	public static final String wiki_kb_mentionsdir= "/save/ngupta19/wikipedia/wiki_kb/mentions_tf/";

	// Directory Path to Annotated Giga .xml.gz
	public static final String agiga_xml_dir = "/save/ngupta19/annotated_gigaword/nyt/";

	// Path to files containing IDS to be ignored
	public static final String agiga_docIDs_ignore1 = "/save/ngupta19/annotated_gigaword/spanish.file-doc.map";
	public static final String agiga_docIDs_ignore2 = "/save/ngupta19/annotated_gigaword/other.file-doc.map";


	// Directory containing AGIGA docs_links text
	public static final String agiga_docsDir = "/save/ngupta19/annotated_gigaword/docs/";
	public static final String agiga_linksDir = "/save/ngupta19/annotated_gigaword/links/";

	// Directory containing AGIGA mention
	public static final String agiga_mentionsDir = "/save/ngupta19/annotated_gigaword/mentions/";

	// CrossWikis lnrm dictionary bz2
	public static final String crosswikis_dict_bz2 = "/save/ngupta19/crosswikis/lnrm.dict.bz2";

	// Crosswikis Map serialized
	public static final String crosswikis_map_ser = "/save/ngupta19/crosswikis/crosswikis.ser";

	// Normalized version of thresholded crosswiki
	public static final String crosswikis_normalized_map_ser = "/save/ngupta19/crosswikis/crosswikis.normalized.ser";

	// Normalized, thresholded crosswikis in file
	public static final String crosswikis_normalized_text = "/save/ngupta19/crosswikis/crosswikis.normalized.text";




	public static final String uiucDataSetRootPath = "/save/ngupta19/WikificationACL2011Data/";
	public static final String processDatasetRootPath = "/save/ngupta19/datasets/";


}
