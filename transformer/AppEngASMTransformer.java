package appeng.transformer;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class AppEngASMTransformer implements IClassTransformer
{

	Multimap<String, String> publicShit = HashMultimap.create();

	public AppEngASMTransformer() {
		publicShit.put( "net.minecraft.client.gui.inventory.GuiContainer", "func_146977_a" );
		publicShit.put( "bcd", "a" );
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if ( publicShit.containsKey( name ) || publicShit.containsKey( transformedName ) )
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader( basicClass );
			classReader.accept( classNode, 0 );

			for (String Set : publicShit.get( name ))
			{
				makePublic( classNode, Set );
			}

			for (String Set : publicShit.get( transformedName ))
			{
				makePublic( classNode, Set );
			}

			// CALL VIRUAL!
			if ( name.equals( "bcd" ) || transformedName.equals( "net.minecraft.client.gui.inventory.GuiContainer" ) )
			{
				for (MethodNode mn : classNode.methods)
				{
					if ( mn.name.equals( "func_146977_a" ) || mn.name.equals( "a" ) )
					{
						MethodNode newNode = new MethodNode( Opcodes.ACC_PUBLIC, "func_146977_a_original", mn.desc, mn.signature, new String[0] );
						newNode.instructions.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );
						newNode.instructions.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );
						newNode.instructions.add( new MethodInsnNode( Opcodes.INVOKESPECIAL, classNode.name, mn.name, mn.desc ) );
						newNode.instructions.add( new InsnNode( Opcodes.RETURN ) );
						classNode.methods.add( newNode );
						break;
					}
				}

				for (MethodNode mn : classNode.methods)
				{
					if ( mn.name.equals( "drawScreen" ) )
					{
						Iterator<AbstractInsnNode> i = mn.instructions.iterator();
						while (i.hasNext())
						{
							AbstractInsnNode in = i.next();
							if ( in.getOpcode() == Opcodes.INVOKESPECIAL )
							{
								MethodInsnNode n = (MethodInsnNode) in;
								if ( n.name.equals( "func_146977_a" ) )
								{
									mn.instructions.insertBefore( n, new MethodInsnNode( Opcodes.INVOKEVIRTUAL, n.owner, n.name, n.desc ) );
									mn.instructions.remove( in );
									break;
								}
							}
						}
					}
				}
			}

			ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
			classNode.accept( writer );
			return writer.toByteArray();
		}

		return basicClass;
	}

	private void makePublic(ClassNode classNode, String set)
	{

		for (MethodNode mn : classNode.methods)
		{
			if ( mn.name.equals( set ) )
			{
				mn.access = getFixedAccess( mn.access );
				return;
			}
		}

		for (FieldNode mn : classNode.fields)
		{
			if ( mn.name.equals( set ) )
			{
				mn.access = getFixedAccess( mn.access );
				return;
			}
		}

	}

	private int getFixedAccess(int access)
	{
		return Opcodes.ACC_PUBLIC;
	}
}
