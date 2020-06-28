from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.parsers import JSONParser, MultiPartParser, FormParser, FileUploadParser
from rest_framework import status
from .serializers import PostMaterialInClassSerializer, MaterialJustDetailsSerializer
from .models import Materials
from notification.views import post_notification

class MaterialsView(APIView):
    '''
    Defines post method to store new materials in the database.
    '''

    parser_classes = [MultiPartParser, JSONParser, FormParser, FileUploadParser]

    def post(self, request):

        '''
        This method stores a material in the @model Materials.
        And pushes a notification to the students of the class

        @param request should contain 'classroom', 'name', 'the_file'
        '''

        serializer = PostMaterialInClassSerializer(data = request.data)

        if serializer.is_valid():
            serializer.save()
            post_notification(classroom = request.data['classroom'], noti_type=1)
            return Response(serializer.data, status = status.HTTP_200_OK)
        else:
            return Response(serializer.errors, status = status.HTTP_400_BAD_REQUEST)


class MaterialsJustDetailsView(APIView):

    '''
    Defines get method to provide materials info of a specific classroom.
    '''

    def get(self, request, classroom):

        '''
        This method returns information about all the materials in a classroom from the @model Materials.
        '''
        
        result = Materials.objects.filter(classroom = classroom)
        serializer = MaterialJustDetailsSerializer(result, many=True)

        return Response(serializer.data, status=status.HTTP_200_OK)


class SingleMaterialView(APIView):
    '''
    Defines get method to provide a specific material info.
    '''

    def get(self, request):

        '''
        This method returns information about a specific material from the @model Materials.
        '''

        mat_id = request.data['id']

        if mat_id is not None:
            if Materials.objects.filter(id = mat_id).exists():
                mat = Materials.objects.get(id = mat_id)
                serializer = MaterialJustDetailsSerializer(mat)
                return Response(serializer.data, status=status.HTTP_200_OK)

            else:
                return Response({"error": "Material does not exist."}, status=status.HTTP_400_BAD_REQUEST)

        else:
            return Response({"error": "Need material id."}, status=status.HTTP_400_BAD_REQUEST)
