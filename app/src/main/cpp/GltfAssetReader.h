#pragma once
#include <cstddef>
#include <cstdint>
#include <string>
namespace nexo { struct GltfInspection { bool valid=false; bool binaryGlb=false; std::string message; std::string json; }; GltfInspection inspectGltf(const std::uint8_t* bytes,std::size_t size); }
