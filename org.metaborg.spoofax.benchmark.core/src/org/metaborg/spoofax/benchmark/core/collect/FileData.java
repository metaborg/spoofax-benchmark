package org.metaborg.spoofax.benchmark.core.collect;

import java.io.Serializable;
import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;

public final class FileData implements Serializable {
	private static final long serialVersionUID = 4589334456774320875L;
	
	public String name;
	public IStrategoTerm ast;
	public final Collection<String> messages = Lists.newLinkedList();
}
