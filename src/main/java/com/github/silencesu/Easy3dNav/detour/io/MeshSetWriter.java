/*
Recast4J Copyright (c) 2015 Piotr Piastucki piotr@jtilia.org

This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.
Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:
1. The origin of this software must not be misrepresented; you must not
 claim that you wrote the original software. If you use this software
 in a product, an acknowledgment in the product documentation would be
 appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/
package com.github.silencesu.Easy3dNav.detour.io;

import com.github.silencesu.Easy3dNav.detour.MeshTile;
import com.github.silencesu.Easy3dNav.detour.NavMesh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

public class MeshSetWriter extends DetourWriter {

	private final MeshDataWriter writer = new MeshDataWriter();
	private final NavMeshParamWriter paramWriter = new NavMeshParamWriter();

	public void write(OutputStream stream, NavMesh mesh, ByteOrder order, boolean cCompatibility) throws IOException {
		// Write header.
		write(stream, NavMeshSetHeader.NAVMESHSET_MAGIC, order);
		write(stream, cCompatibility ? NavMeshSetHeader.NAVMESHSET_VERSION : NavMeshSetHeader.NAVMESHSET_VERSION_RECAST4J, order);
		int numTiles = 0;
		for (int i = 0; i < mesh.getMaxTiles(); ++i) {
			MeshTile tile = mesh.getTile(i);
			if (tile == null || tile.data == null || tile.data.header == null)
				continue;
			numTiles++;
		}
		write(stream, numTiles, order);
		paramWriter.write(stream, mesh.getParams(), order);

		// Store tiles.
		for (int i = 0; i < mesh.getMaxTiles(); ++i) {
			MeshTile tile = mesh.getTile(i);
			if (tile == null || tile.data == null || tile.data.header == null)
				continue;

			NavMeshTileHeader tileHeader = new NavMeshTileHeader();
			tileHeader.tileRef = mesh.getTileRef(tile);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			writer.write(baos, tile.data, order, cCompatibility);
			byte[] ba = baos.toByteArray();
			tileHeader.dataSize = ba.length;
			write(stream, tileHeader.tileRef, order);
			write(stream, tileHeader.dataSize, order);
			if (cCompatibility) {
				write(stream, 0, order); // C struct padding
			}
			stream.write(ba);
		}
	}

}
