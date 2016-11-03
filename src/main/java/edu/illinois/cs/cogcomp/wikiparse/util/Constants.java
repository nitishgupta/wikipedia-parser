package edu.illinois.cs.cogcomp.wikiparse.util;

/**
 * Created by nitishgupta on 10/24/16.
 */
public class Constants {
	// mid \t alias/name - From freebase for all mids
	public static final String mid_alias_names_file = "/save/ngupta19/freebase/types_pruned/mid.alias.names";

	// mid \t fb_name \t wid - For all relevant types freebase entities
	public static final String mid_names_wid_filepath = "/save/ngupta19/freebase/types_pruned/mid.names.wiki_id_en";

	// wid - wids from freebase that are found in Wiki and not redirect/disambiguation
	public static final String wid_foundInWiki_filepath = "/save/ngupta19/freebase/types_pruned/wid.foundInWiki";

	// wid \t WikiTitle for final wids
	public static final String wid_WikiTitle_filepath = "/save/ngupta19/freebase/types_pruned/wid.WikiTitle";

	// Directory containing all docs_links text
	public static final String wiki_kb_docsDir = "/save/ngupta19/wikipedia/wiki_kb_10_15/docs_links/";

	// Filepath to file containing wikipedia mentions
	public static final String wiki_kb_mentionsFile = "/save/ngupta19/wikipedia/wiki_kb_10_15/mentions.txt";

	// Directory Path to Annotated Giga .xml.gz
	public static final String agiga_xml_dir = "/save/ngupta19/annotated_gigaword/nyt/";

	// Directory containing AGIGA docs_links text
	public static final String agiga_docsDir = "/save/ngupta19/annotated_gigaword/docs_links/";





}
