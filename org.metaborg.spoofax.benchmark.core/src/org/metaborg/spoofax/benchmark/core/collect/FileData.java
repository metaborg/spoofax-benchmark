package org.metaborg.spoofax.benchmark.core.collect;

import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;

public final class FileData {
	public String name;
	public IStrategoTerm ast;
	public final Collection<String> messages = Lists.newLinkedList();
}
