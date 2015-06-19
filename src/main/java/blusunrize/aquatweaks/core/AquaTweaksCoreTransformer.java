package blusunrize.aquatweaks.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import blusunrize.aquatweaks.FluidUtils;
import blusunrize.aquatweaks.RenderWorldEventMid;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class AquaTweaksCoreTransformer implements IClassTransformer
{
	@Override
	public byte[] transform(String className, String newClassName, byte[] origCode)
	{
		//patch shouldSideBeRendered in BlockLiquid
		if(className.equals("net.minecraft.block.BlockLiquid")||className.equals("alw"))
		{
			ClassReader rd = new ClassReader(origCode);
			ClassWriter wr = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor patcher = new Visitor_ShouldSide(wr);
			rd.accept(patcher, ClassReader.EXPAND_FRAMES);
			return wr.toByteArray();
		}

		//add custom render hook
		if(className.equals("net.minecraft.client.renderer.WorldRenderer")||className.equals("blo"))
		{
			ClassReader rd = new ClassReader(origCode);
			ClassWriter wr = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor patcher = new Visitor_RenderEvent(wr);
			rd.accept(patcher, ClassReader.EXPAND_FRAMES);
			return wr.toByteArray();
		}
		return origCode;
	}

	private static class InsertInitCodeBeforeReturnMethodVisitor extends MethodVisitor
	{
		public InsertInitCodeBeforeReturnMethodVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitInsn(int opcode)
		{
			if(opcode==Opcodes.RETURN)
			{
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ILOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
						"blusunrize/aquatweaks/core/AquaTweaksCoreTransformer",
						"fireMidRenderEvent",
							"(Lnet/minecraft/client/renderer/WorldRenderer;I)V",
								false);
			}
			super.visitInsn(opcode);
		}
	}

	public static class Visitor_RenderEvent extends ClassVisitor
	{
		public Visitor_RenderEvent(ClassWriter writer)
		{
			super(Opcodes.ASM4, writer);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
		{
			final String methodToPatch = "preRenderBlocks";
			final String methodToPatch_srg = "func_147890_b";
			final String methodToPatch_obf = "b";
			final String qdesc = "(I)V";
			if((name.equals(methodToPatch)||name.equals(methodToPatch_srg)||name.equals(methodToPatch_obf))
					&&(desc.equals(qdesc)))
			{
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				return new InsertInitCodeBeforeReturnMethodVisitor(mv);

			}
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
	}
	public static void fireMidRenderEvent(WorldRenderer wr, int pass)
	{
		RenderBlocks rb = ObfuscationReflectionHelper.getPrivateValue(ForgeHooksClient.class,null, "worldRendererRB");
		if(rb!=null && rb.blockAccess instanceof ChunkCache)
			MinecraftForge.EVENT_BUS.post(new RenderWorldEventMid(wr, (ChunkCache)rb.blockAccess, rb, pass));
	}


	public static class Visitor_ShouldSide extends ClassVisitor
	{
		public Visitor_ShouldSide(ClassWriter writer)
		{
			super(Opcodes.ASM4, writer);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
		{
			final String methodToPatch = "shouldSideBeRendered";
			final String methodToPatch_srg = "func_149646_a";
			final String methodToPatch_obf = "a";
			final String qdesc = "(Lnet/minecraft/world/IBlockAccess;IIII)Z";
			final String qdesc_obf = "(Lahl;IIII)Z";
			final String qdescInv = "(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;IIII)Z";
			final String qdescInv_obf = "(Laji;Lahl;IIII)Z";

			if((name.equals(methodToPatch)||name.equals(methodToPatch_srg)||name.equals(methodToPatch_obf))
					&&(desc.equals(qdesc)||desc.equals(qdesc_obf)))
			{
				final String invokeDesc = desc.equals(desc)?qdescInv:qdescInv_obf;

				return new MethodVisitor(Opcodes.ASM4, super.visitMethod(access, name, desc, signature, exceptions))
				{
					@Override
					public void visitCode()
					{
						mv.visitCode();
						mv.visitVarInsn(Opcodes.ALOAD, 0);

						mv.visitVarInsn(Opcodes.ALOAD, 1);
						mv.visitVarInsn(Opcodes.ILOAD, 2);
						mv.visitVarInsn(Opcodes.ILOAD, 3);
						mv.visitVarInsn(Opcodes.ILOAD, 4);
						mv.visitVarInsn(Opcodes.ILOAD, 5);
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "blusunrize/aquatweaks/core/AquaTweaksCoreTransformer", "liquid_shouldSideBeRendered",
								invokeDesc, false);
						mv.visitInsn(Opcodes.IRETURN);
						mv.visitMaxs(5, 1);
						mv.visitEnd();
					}
				};
			}
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
	}
	public static boolean liquid_shouldSideBeRendered(Block block, IBlockAccess world, int x, int y, int z, int side)
	{
		if(side>=0 && side<6)
			if(FluidUtils.canFluidConnectToBlock(world, x, y, z, side, block.getMaterial()))
//			if(world.getBlock(x, y, z) instanceof IAquaConnectable && ((IAquaConnectable)world.getBlock(x, y, z)).canConnectTo(world, x, y, z, ForgeDirection.OPPOSITES[side]) && FluidUtils.isBlockSubmerged(world, x, y, z, Material.water))
				return false;
		Material material = world.getBlock(x, y, z).getMaterial();
		return material == block.getMaterial() ? false : (side == 1 ? true : 
			side==0&&block.getBlockBoundsMinY()>0?true: (side==1&&block.getBlockBoundsMaxY()<1?true: (side==2&&block.getBlockBoundsMinZ()>0?true: (side==3&&block.getBlockBoundsMaxZ()<1?true: (side==4&&block.getBlockBoundsMinX()>0?true: (side==5&&block.getBlockBoundsMaxX()<1?true : !world.getBlock(x,y,z).isOpaqueCube()))))));
	}
}